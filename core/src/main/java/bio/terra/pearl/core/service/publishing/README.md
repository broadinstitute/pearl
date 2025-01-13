Publishing

In Juniper, the standard practice for configurations is that they are created in a sandbox environment, and then
published to irb/prod environments once they have been tested.
For configuration objects that should be published across environments, they should implement the StudyEnvPublishable or PortalEnvPublishable
interface depending on where they are attached.  Once they implement these interfaces, they will 
automatically be including in diffing and publishing operations.  

Note that implementing that interface is not enough to make the object appear in the 
diff/publish UX.  Any changes to PortalEnvironmentChange will require updates to the UX to allow admin users
to view and select changes to publish
