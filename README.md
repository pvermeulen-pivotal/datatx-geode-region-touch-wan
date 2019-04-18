# Geode-Region-Wan-Touch

The purpose of the Geode-Region-Wan-Touch application is to provide a mechanism that touches/updates all objects in a region 
or regions in order update region(s) entries on one or more WAN endpoints.

The touch mechanism supports java, PDX and data serializable objects. 

### RegionTouch Function
The RegionTouch function is the interface used to call the InternalRegionTouch function which is responsible for performing 
the touching/updating of all objects in a region. Additionally, if the region or regions specified in the function call are
not configured to have a gateway sender, then the function will bypass touching/updating the region entries.

Since the RegionTouch function invokes the InternalRegionTouch function, the RegionTouch function should only be executed on 
a single member and an exception will occur if the region or onRegion option is specified on the function call. 

The RegionTouch function supports touching/updating all regions, a list of regions or a single region.

The arguments parameter on the function call is used to define the region or regions to touch. The special name ALL is used 
to touch all regions in the cluster.

#### GFSH Examples
1.	execute function --id RegionTouch --arguments region1 --member server01 
2.	execute function --id RegionTouch --arguments region1,region2,region3 --member server01
3.	execute function --id RegionTouch --arguments all --member server01

### Deployment
The functions can be deployed using GFSH or defined in cache or Spring context functions section and the jar must be on 
the classpath when using a context.

#### GFSH Deployment
deploy --jar region-wan-touch-1.0.0-RELEASE.jar 
