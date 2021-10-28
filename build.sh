#!/bin/bash

# Script to build CoatRack using Maven

# Please note: 
# - a CoatRack build requires some mail configuration parameters to be set
# - the configuration values in this file are just dummy/fallback values for development builds

mvn clean package -Dygg.mail.sender.user="" -Dygg.mail.sender.password="" -Dygg.mail.server.url="" -Dygg.mail.server.port=0 -Dygg.mail.sender.from="" -Dygg.proxy.executable-jar.template.url="" -Dygg.proxy.executable-jar.generated.path=""
