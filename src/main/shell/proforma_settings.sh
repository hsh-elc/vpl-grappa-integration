#!/bin/bash

# Middleware settings
# All values need to be put inside single-quotes (Example: SERVICE_URL='https://example.com/proforma-webservice')

# URL of the middleware service and the LMS credentials. 
# All mandatory.
export SERVICE_URL=<VPL_PROFORMA_SERVICE_URL>
export LMS_ID=<VPL_PROFORMA_LMS_ID>
export LMS_PASSWORD=<VPL_PROFORMA_LMS_PASSWORD>

# Accept self signed certificates of the middleware server. 
# Optional setting.
# Accepted values: YES, NO, TRUE, FALSE, ON, OFF (case doesn't matter)
# Default is NO
export ACCEPT_SELF_SIGNED_CERTS=<VPL_PROFORMA_ACCEPT_SELF_SIGNED_CERTS>

# Grader settings

# Name of the grader - options include Graja, aSQLg, GraFlap, etc.
# Mandatory
export GRADER_NAME=<VPL_PROFORMA_GRADER_NAME>

# Version of the specified grader
# Mandatory
export GRADER_VERSION=<VPL_PROFORMA_GRADER_VERSION>

# Submission settings

# Feedback format
# Optional setting. 
# Options: "zip" or "xml". 
# Default is "zip"
export FEEDBACK_FORMAT=<VPL_PROFORMA_FEEDBACK_FORMAT>

# Feedback structure 
# Optional setting. 
# Options: "merged-test-feedback" or "separate-test-feedback".
# Default is "separate-test-feedback"
export FEEDBACK_STRUCTURE=<VPL_PROFORMA_FEEDBACK_STRUCTURE>

# Student feedback level
# Options: "debug", "info", "warn", "error"
# Optional setting. 
# Default is "info"
export STUDENT_FEEDBACK_LEVEL=<VPL_PROFORMA_STUDENT_FEEDBACK_LEVEL>

# Teacher feedback level 
# Options: "debug", "info", "warn", "error"
# Optional setting. 
# Default is "info"
export TEACHER_FEEDBACK_LEVEL=<VPL_PROFORMA_TEACHER_FEEDBACK_LEVEL>
