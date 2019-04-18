package util.geode.region.wan.function;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.execute.ResultCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class RegionTouch implements Function, Declarable {
	private static final Logger LOG = LoggerFactory.getLogger(RegionTouch.class);

	private int updates = 0;
	private Set<String> regionNames;
	private Cache cache;

	public void execute(FunctionContext context) {
		if (context instanceof RegionFunctionContext)
			throw new FunctionException(
					"Function " + this.getClass().getSimpleName() + " does not support onRegion or gfsh --region");

		cache = CacheFactory.getAnyInstance();
		regionNames = new HashSet<String>();
		updates = 0;

		String[] args = (String[]) context.getArguments();
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if ("ALL".equals(arg.toUpperCase())) {
					Set<Region<?, ?>> regions = cache.rootRegions();
					regions.stream().forEach(region -> regionNames.add(region.getFullPath()));
					break;
				} else {
					if (arg != null && arg.length() > 0) {
						regionNames.add(arg);
					}
				}
			}
		}
		
		if (regionNames.size() > 0) {
			processRegions();
			LOG.info(regionNames.size() + " regions and " + updates + " objects were touched");
			context.getResultSender()
					.lastResult(regionNames.size() + " regions and " + updates + " objects were touched");
		} else {
			LOG.warn(this.getClass().getSimpleName() + " function - No regions names passed as arguments");
			context.getResultSender()
					.lastResult(this.getClass().getSimpleName() + " function - No regions names passed as arguments");
		}
	}

	private void processRegions() {
		for (String regionName : regionNames) {
			long regionUpdates = 0;
			Region region = cache.getRegion(regionName);
			if (region != null) {
				Execution exec = FunctionService.onRegion(region);
				ResultCollector rc = exec.execute("InternalRegionTouch");
				List<Integer> results = (List<Integer>) rc.getResult();
				if (results != null) {
					for (Integer update : results) {
						updates = updates + update;
						regionUpdates = regionUpdates + update;
					}
				}
				LOG.info("Processed region " + regionName + ": Objects touched = " + regionUpdates);
			} else {
				LOG.warn("No region found for " + regionName);
			}
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