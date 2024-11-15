package bio.terra.pearl.core.model.log;

public enum LogEventType {
  ERROR, // an error occurred
  WARN, // something that might be a concern, but does not require action
  ACCESS, // someone requesting a resource
  EVENT, // an ApplicationEvent was fired
  STATS, // stats measurement -- e.g. webVitals
  INFO // something interesting that isn't an error
}
