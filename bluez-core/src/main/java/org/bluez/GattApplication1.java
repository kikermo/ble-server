package org.bluez;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public interface GattApplication1 extends DBusInterface {
	public Map<DBusPath, Map<String, Map<String, Variant>>> GetManagedObjects();
}
