package io.github.lordscales91.magic9.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a 3DS System Firmware. It's used to determine the right hacking path 
 * and provides methods to compare versions
 */
public class FirmwareVersion {
	
	public static final FirmwareVersion LATEST_HACKABLE = new FirmwareVersion(11, 4, 0);
	
	private static final String VERSION_PATT = "([0-9]+)\\.([0-9]+)\\.([0-9]+)-([0-9]+)([a-zA-Z])";
	private static final String SHORT_PATT = "([0-9]+)\\.([0-9]+)\\.([0-9]+)";
	
	private int major;
	private int minor;
	private int patch;
	private int browser;
	private ConsoleRegion region = ConsoleRegion.OTHER;
	private ConsoleModel model = ConsoleModel.O3DS_GENERIC;
	
	public FirmwareVersion(String version) {
		this(version, ConsoleModel.O3DS_GENERIC);
	}
	
	@Deprecated
	public FirmwareVersion(String version, String model) {
		this(version, ConsoleModel.fromModelType(model));
	}
	
	public FirmwareVersion(String version, ConsoleModel model) {
		Matcher m = Pattern.compile(VERSION_PATT).matcher(version);
		if(m.matches()) {
			major = Integer.parseInt(m.group(1));
			minor = Integer.parseInt(m.group(2));
			patch = Integer.parseInt(m.group(3));
			browser = Integer.parseInt(m.group(4));
			region = ConsoleRegion.fromFirmware(m.group(5));
		} else {
			m = Pattern.compile(SHORT_PATT).matcher(version);
			if(m.matches()) {
				major = Integer.parseInt(m.group(1));
				minor = Integer.parseInt(m.group(2));
				patch = Integer.parseInt(m.group(3));
			} else {
				throw new IllegalArgumentException("invalid version: "+version);
			}
		}
		this.model = model;
	}

	public FirmwareVersion(int major, int minor, int patch, int browser,
			ConsoleRegion region, String model) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.browser = browser;
		this.region = region;
		this.setModel(model);
	}
	
	public FirmwareVersion(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	public FirmwareVersion(int major, int minor, int patch, int browser) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.browser = browser;
	}

	public boolean lt(FirmwareVersion other) {
		if(this.major < other.major) {
			return true;
		}
		if(this.major == other.major) {
			if(this.minor < other.minor) {
				return true;
			} else if (this.minor == other.minor && this.patch < other.patch) {
				return true;
			}
		}
		return false;
	}
	public boolean lt(String version) {
		return this.lt(new FirmwareVersion(version));
	}
	public boolean systemVersionEqual(FirmwareVersion other) {
		return systemVersionEqual(other, false);
	}
	
	public boolean systemVersionEqual(FirmwareVersion other, boolean considerpatch) {
		return this.major == other.major && this.minor == other.minor &&
				((considerpatch)?this.patch == other.patch:true);
	}
	public boolean lte(FirmwareVersion other) {
		return this.lte(other, false);
	}
	
	public boolean lte(FirmwareVersion other, boolean considerpatch) {
		return this.lt(other) || this.systemVersionEqual(other, considerpatch);
	}
	
	public boolean gt(FirmwareVersion other) {
		if(this.major > other.major) {
			return true;
		}
		if(this.major == other.major) {
			if(this.minor > other.minor) {
				return true;
			} else if (this.minor == other.minor && this.patch > other.patch) {
				return true;
			}
		}
		return false;
	}
	
	public boolean gte(FirmwareVersion other) {
		return this.gte(other, false);
	}
	
	public boolean gte(FirmwareVersion other, boolean considerpatch) {
		return this.gt(other) || this.systemVersionEqual(other, considerpatch);
	}

	public boolean gte(String version) {
		return this.gte(new FirmwareVersion(version));
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public int getPatch() {
		return patch;
	}

	public void setPatch(int patch) {
		this.patch = patch;
	}

	public int getBrowser() {
		return browser;
	}

	public void setBrowser(int browser) {
		this.browser = browser;
	}

	public ConsoleRegion getRegion() {
		return region;
	}

	public void setRegion(ConsoleRegion region) {
		this.region = region;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}
	
	@Deprecated
	public String getModel() {
		return model.modelType();
	}
	
	public ConsoleModel getConsoleModel() {
		return model;
	}

	@Deprecated
	public void setModel(String model) {
		setModel(ConsoleModel.fromModelType(model));
	}
	
	public void setModel(ConsoleModel model) {
		this.model = model;
	}
	
	/**
	 * Returns the short string representation of this version.
	 * The format is: major.minor.patch
	 */
	public String toShortVersion() {
		return String.format("%d.%d.%d", major, minor, patch);
	}

	@Override
	public String toString() {
		return String.format("%d.%d.%d-%d%c %s", major, minor, patch,
				browser, region.toLetter(), model);
	}
}
