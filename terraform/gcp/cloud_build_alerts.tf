resource "google_logging_metric" "cloud_build_error_logging_metric" {
  name   = "juniper-deploy-failures/metric"
  filter = <<EOT
resource.type="build"
severity>=ERROR
EOT
  metric_descriptor {
    metric_kind = "DELTA"
    value_type  = "INT64"
  }
}


resource "google_monitoring_alert_policy" "cloud_build_error_alert_policy" {
  count = var.slack_notification_channel != "" ? 1 : 0

  display_name = "juniper-deploy-error-alert"
  notification_channels = [var.slack_notification_channel]
  conditions {
    display_name = "Deploment Failure"
    condition_threshold {
      filter     = "metric.type=\"logging.googleapis.com/user/juniper-deploy-failures/metric\" AND resource.type=\"build\""
      comparison = "COMPARISON_GT"
      threshold_value = 0
      duration = "0s"
      trigger {
        count = 1
      }
    }
  }
  combiner = "AND"
  severity = "ERROR"

  documentation {
    mime_type = "text/markdown"
    subject = "Deployment Failed"
    content = "See build history: https://console.cloud.google.com/cloud-build/builds;project=${var.project}"
  }
  alert_strategy {
    notification_prompts = ["OPENED"]
  }

  depends_on = [
    google_logging_metric.cloud_build_error_logging_metric
  ]
}


# The following alerts are not currently used because of the fact that
# alerts are currently very noisy and will always be categorized as
# errors.
#
# If we want to enable these, we'll need to figure out how to customize
# the message content to make them more compact and meaningful.
# It could be possible to customize the messages with:
# https://medium.com/ordergroove-engineering/posting-slack-notifications-using-google-cloud-function-900ed2f1e3ec
#
# resource "google_logging_metric" "cloud_build_success_logging_metric" {
#   name   = "juniper-deploy-success/metric"
#   filter = <<EOT
# resource.type="build"
# SEARCH("Deployment complete for tag")
# EOT
#   metric_descriptor {
#     metric_kind = "DELTA"
#     value_type  = "INT64"
#   }
# }
#
# resource "google_monitoring_alert_policy" "cloud_build_success_alert_policy" {
#   count = var.slack_notification_channel != "" ? 1 : 0
#
#   display_name = "juniper-deploy-success-alert"
#   notification_channels = [var.slack_notification_channel]
#   conditions {
#     display_name = "Build Success"
#     condition_threshold {
#       filter     = "metric.type=\"logging.googleapis.com/user/juniper-deploy-success/metric\" AND resource.type=\"build\""
#       comparison = "COMPARISON_GT"
#       threshold_value = 0
#       duration = "0s"
#       trigger {
#         count = 1
#       }
#     }
#   }
#
#   documentation {
#     mime_type = "text/markdown"
#     subject = "Deployment Succeeded"
#     content = "See [build history](https://console.cloud.google.com/cloud-build/builds;project=${var.project})"
#   }
#
#   combiner = "AND"
#
#   alert_strategy {}
#
#   depends_on = [
#     google_logging_metric.cloud_build_error_logging_metric
#   ]
# }
#
# resource "google_logging_metric" "cloud_build_started_logging_metric" {
#   name   = "juniper-deploy-started/metric"
#   filter = <<EOT
# resource.type="build"
# SEARCH("Starting deployment for tag")
# EOT
#   metric_descriptor {
#     metric_kind = "DELTA"
#     value_type  = "INT64"
#   }
# }
#
# resource "google_monitoring_alert_policy" "cloud_build_started_alert_policy" {
#   count = var.slack_notification_channel != "" ? 1 : 0
#
#   display_name = "juniper-deploy-success-alert"
#   notification_channels = [var.slack_notification_channel]
#   conditions {
#     display_name = "Build Success"
#     condition_threshold {
#       filter     = "metric.type=\"logging.googleapis.com/user/juniper-deploy-started/metric\" AND resource.type=\"build\""
#       comparison = "COMPARISON_GT"
#       threshold_value = 0
#       duration = "0s"
#       trigger {
#         count = 1
#       }
#
#     }
#   }
#   combiner = "AND"
#
#   documentation {
#     mime_type = "text/markdown"
#     subject = "Deployment Started"
#     content = "See [build history](https://console.cloud.google.com/cloud-build/builds;project=${var.project})"
#   }
#
#   alert_strategy {}
#
#   depends_on = [
#     google_logging_metric.cloud_build_error_logging_metric
#   ]
# }
#
