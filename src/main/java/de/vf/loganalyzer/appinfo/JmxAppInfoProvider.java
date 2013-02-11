package de.vf.loganalyzer.appinfo;

import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.springframework.beans.factory.annotation.Required;

public class JmxAppInfoProvider implements AppInfoProvider {
    private MBeanServerConnection _connection;

    private String _domain;

    @Override
    public String getContentType() {
        return "text/plain";
    }

    public String getAppInfo() {
        try {
            StringBuilder result = new StringBuilder();

            Set<ObjectName> moduleInfoNames = _connection.queryNames(new ObjectName(_domain + ":type=*ModuleInfo"), null);
            for (ObjectName moduleInfoName : moduleInfoNames) {
                result.append(moduleInfoName).append("\n");

                MBeanInfo moduleInfo = _connection.getMBeanInfo(moduleInfoName);
                MBeanAttributeInfo[] attributes = moduleInfo.getAttributes();
                for (MBeanAttributeInfo attribute : attributes) {
                    result.append(attribute.getName())
                            .append(":")
                            .append(_connection.getAttribute(moduleInfoName, attribute.getName()))
                            .append("\n");
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Required
    public void setConnection(MBeanServerConnection connection) {
        _connection = connection;
    }

    @Required
    public void setDomain(String domain) {
        _domain = domain;
    }
}
