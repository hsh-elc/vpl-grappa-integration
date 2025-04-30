#!/bin/bash

# Middleware settings

# URL of the middleware service and the LMS credentials. 
# All mandatory.
export SERVICE_URL="http://grappadev.inform.hs-hannover.de:8080/grappa-webservice-2/rest/"
export LMS_ID="test"
export LMS_PASSWORD="test"

# Accept self signed certificates of the middleware server. 
# Optional setting.
# Accepted values: YES, NO, TRUE, FALSE, ON, OFF (case doesn't matter)
# Default is NO
export ACCEPT_SELF_SIGNED_CERTS="YES"

# Grader settings

# Name of the grader - options include Graja, aSQLg, GraFlap, etc.
# Mandatory
export GRADER_NAME="Graja"

# Version of the specified grader
# Mandatory
export GRADER_VERSION="2.3"

# Submission settings

# Feedback format
# Optional setting. 
# Options: "zip" or "xml". 
# Default is "zip"
export FEEDBACK_FORMAT="zip"

# Feedback structure 
# Optional setting. 
# Options: "merged-test-feedback" or "separate-test-feedback".
# Default is "separate-test-feedback"
export FEEDBACK_STRUCTURE="merged-test-feedback"

# Student feedback level
# Options: "debug", "info", "warn", "error"
# Optional setting. 
# Default is "info"
export STUDENT_FEEDBACK_LEVEL="debug"

# Teacher feedback level 
# Options: "debug", "info", "warn", "error"
# Optional setting. 
# Default is "info"
export TEACHER_FEEDBACK_LEVEL="debug"
