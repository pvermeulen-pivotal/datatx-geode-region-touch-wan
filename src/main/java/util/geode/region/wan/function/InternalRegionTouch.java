package util.geode.region.wan.function;

import java.util.Properties;
import java.util.Set;

import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class InternalRegionTouch implements Function, Declarable {
	private static final Logger LOG = LoggerFactory.getLogger(InternalRegionTouch.class);

	public void execute(FunctionContext context) {
		if (context instanceof RegionFunctionContext) {
			Set<Object> keys = null;
			Region region = ((RegionFunctionContext) context).getDataSet();
			if (region != null) {
				Set<String> gatewayIds = region.getAttributes().getGatewaySenderIds();
				if (gatewayIds != null && gatewayIds.size() > 0) {
					if (PartitionRegionHelper.isPartitionedRegion(region)) {
						Region localRegion = PartitionRegionHelper.getLocalPrimaryData(region);
						keys = (Set<Object>) localRegion.keySet();
					} else {
						keys = (Set<Object>) region.keySet();
					}
					for (Object key : keys) {
						Object value = region.get(key);
						region.put(key, value);
					}
				} else {
					LOG.warn(region.getFullPath() + " region does not have gateway sender id(s) assigned");
				}
			}
			context.getResultSender().lastResult(keys.size());
		} else {
			throw new FunctionException(this.getClass().getSimpleName() + " must be called using onRegion");
		}
	}

	public String getId() {
		return getClass().getSimpleName();
	}

	public boolean optimizeForWrite() {
		return true;
	}

	public boolean hasResult() {
		return true;
	}

	public boolean isHA() {
		return true;
	}

	public void init(Properties properties) {
	}
}
