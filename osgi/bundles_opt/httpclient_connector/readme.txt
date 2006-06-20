This is a ConnectionFactory which are typically used together with the
IO-Connector. The IO-Connector provides a convenient way to open
connections to remote hosts and communicate with these using the HTTP.

This ConnectionFactory like another ConnectionFactory distributed 
with KF supports the http-scheme (see the connectors bundle). These
two connectors *should* be more or less equivalent, except for that
this ConnectionFactory can be configured to work with proxies. If your
application is to run in an environment where you do not need this 
functionality you may want to use other connector. This to avoid
an unnecessary dependency to the commons-logging bundle (which is 
found in ../commons-logging). To build and use this bundle you need to 
build and install the commons-logging bundle.

This bundle introduces a few (optional) properties:

org.knopflerfish.httpclient_connector.proxy.server=<host> 
org.knopflerfish.httpclient_connector.proxy.port=<int> 

Some proxies require authenication. The following properties allow
you to set up the connector properly in such environments. (Requires 
the proxy server and port to be set)

org.knopflerfish.httpclient_connector.username=<username>
org.knopflerfish.httpclient_connector.password=<password>

org.knopflerfish.httpclient_connector.realm=<realm>   (optional)
org.knopflerfish.httpclient_connector.scheme=<scheme> (optional)

You can also set SO_TIMEOUT using
org.knopflerfish.httpclient_connector.so_timeout=<int> 

To disable HTTPS support set 
org.knopflerfish.httpclient_connector.https_enabled to "false"

