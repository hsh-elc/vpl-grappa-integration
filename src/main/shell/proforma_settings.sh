#!/bin/bash

# Middleware settings
# URL of the middleware service and the LMS credentials
export SERVICE_URL="http://grappadev.inform.hs-hannover.de:8080/grappa-webservice-2/rest/"
export LMS_ID="test"
export LMS_PASSWORD="test"

# Grader settings
# Name of the grader - options include Graja, aSQLg, GraFlap, etc.
export GRADER_NAME="Graja"
# Version of the specified grader
export GRADER_VERSION="2.3"

# Submission settings
# Feedback format - options: "zip" or "xml"
export FEEDBACK_FORMAT="zip"
# Feedback structure - options: "merged-test-feedback" or "separate-test-feedback"
export FEEDBACK_STRUCTURE="merged-test-feedback"
# Student feedback level - options: "debug", "info", "warn", "error"
export STUDENT_FEEDBACK_LEVEL="debug"
# Teacher feedback level - options: "debug", "info", "warn", "error"
export TEACHER_FEEDBACK_LEVEL="debug"
