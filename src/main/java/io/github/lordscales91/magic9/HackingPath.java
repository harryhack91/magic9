package io.github.lordscales91.magic9;

import io.github.lordscales91.magic9.core.HackingProcess;
import io.github.lordscales91.magic9.core.HackingResource;
import io.github.lordscales91.magic9.domain.ConsoleRegion;
import io.github.lordscales91.magic9.domain.FirmwareVersion;
import io.github.lordscales91.magic9.domain.HackingStep;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class HackingPath {
	
	private static HackingPath INSTANCE;
	private static final FirmwareVersion[][] O3DS_RANGES = new FirmwareVersion[][]{
		// From, To
		new FirmwareVersion[]{
			// No Browser | Browser
			// No option available shown on 3ds.guide
			new FirmwareVersion(1, 0, 0), new FirmwareVersion(1, 1, 0)
		},
		new FirmwareVersion[]{
			// - | Install boot9strap (2xrsa)
			new FirmwareVersion(2, 1, 0), new FirmwareVersion(2, 1, 0)
		},
		new FirmwareVersion[]{
			// No option available shown on 3ds.guide
			new FirmwareVersion(2, 2, 0), new FirmwareVersion(3, 1, 0)
		},
		new FirmwareVersion[]{
			// Install boot9strap (MSET) | Install boot9strap (Browser)
			new FirmwareVersion(4, 0, 0), new FirmwareVersion(4, 5, 0)
		},
		new FirmwareVersion[]{
			// - | Install boot9strap (Browser)
			new FirmwareVersion(5, 0, 0), new FirmwareVersion(5, 1, 0)
		},
		new FirmwareVersion[]{
			// Install boot9strap (MSET) | Install boot9strap (Browser)
			new FirmwareVersion(6, 0, 0), new FirmwareVersion(6, 3, 0)
		},
		new FirmwareVersion[]{
			// - | Install boot9strap (Browser)
			new FirmwareVersion(7, 0, 0), new FirmwareVersion(8, 1, 0)
		},
		new FirmwareVersion[]{
			// Homebrew Launcher(Soundhax)
			new FirmwareVersion(9, 0, 0), new FirmwareVersion(11, 3, 0)
		},
		new FirmwareVersion[]{
			// Install boot9strap (DSiWare)
			new FirmwareVersion(11, 4, 0), new FirmwareVersion(11, 4, 0)
		}
	};
	private static final FirmwareVersion[][] N3DS_RANGES = new FirmwareVersion[][]{
		new FirmwareVersion[]{
			// NTR and Cubic Ninja (JPN Only).
			new FirmwareVersion(8, 1, 0), new FirmwareVersion(8, 1, 0)
		},
		new FirmwareVersion[]{
			// Homebrew Launcher(Soundhax)
			new FirmwareVersion(9, 0, 0), new FirmwareVersion(11, 3, 0)
		},
		new FirmwareVersion[]{
			// Install boot9strap (DSiWare)
			new FirmwareVersion(11, 4, 0), new FirmwareVersion(11, 4, 0)
		}
	};
	
	public static final Properties URLS = loadURLs();
	
	private static Properties loadURLs() {
		Properties props = new Properties();
		try {
			FileReader fr = new FileReader("data/urls.properties");
			props.load(fr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}
	
	private List<HackingStep> steps;
	private int currentStepIndex;
	private FirmwareVersion fwver;
	private boolean cartUpdated;
	private String hackingDir;
	private String sdCardDir;
	private HackingProcess currentProcess;
	
	public static HackingPath resolve(FirmwareVersion fwver, boolean cartUpdated) {
		if(fwver == null) {
			throw new IllegalArgumentException("Firmware was not specified");
		}
		if(INSTANCE == null) {
			INSTANCE = new HackingPath();
		}		
		INSTANCE.setFwver(fwver);
		INSTANCE.setCartUpdated(cartUpdated);
		INSTANCE.init();
		return INSTANCE;
	}
	
	public static HackingPath getInstance() {
		if(INSTANCE == null) {
			throw new IllegalStateException("HackingPath has not been initialized");
		}
		return INSTANCE;
	}

	private HackingPath(){}
	
	private void init() {
		steps = new ArrayList<HackingStep>();
		if(!this.fwver.getConsoleModel().isNew()) {
			int range = -1;
			for(int i=0;i<O3DS_RANGES.length;i++) {
				FirmwareVersion from = O3DS_RANGES[i][0];
				FirmwareVersion to = O3DS_RANGES[i][1];
				if(this.fwver.gte(from) && this.fwver.lte(to)) {
					range = i;
					i = O3DS_RANGES.length;
				}
			}
			boolean usablebrowser = isBrowserUsable();
			
			switch(range) {
				case 0:
				case 2:
					steps.add(HackingStep.NO_OPTION);
					break;
				case 1:
					if(usablebrowser) {
						// Proceed directly to install boot9strap
						steps.add(HackingStep.BOOT9STRAP_2XRSA);
					} else {
						steps.add(HackingStep.NO_OPTION);
					}
					break;
				case 3:
				case 5:
					if(usablebrowser) {
						// Proceed to install boot9strap this way
						steps.add(HackingStep.BOOT9STRAP_BROWSER);
					} else {
						// Use mset hax
						steps.add(HackingStep.BOOT9STRAP_MSET);						
					}
					steps.add(HackingStep.BOOT9STRAP_FINAL_SETUP);
					break;
				case 4:
				case 6:
					if(usablebrowser) {
						// Proceed to install boot9strap this way
						steps.add(HackingStep.BOOT9STRAP_BROWSER);
						steps.add(HackingStep.BOOT9STRAP_FINAL_SETUP);
					} else {
						// Require an update
						steps.add(HackingStep.NO_OPTION);						
					}
					break;
				case 7:
					if(ConsoleRegion.KOR.equals(fwver.getRegion()) && fwver.lt("9.6.0")) {
						// Soundhax on Korean consoles only works on version 9.6.0 or higher
						steps.add(HackingStep.REQUIRES_UPDATE);
					} else {
						steps.add(HackingStep.HOMEBREW_SOUNDHAX);
						steps.add(HackingStep.BOOT9STRAP_HOMEBREW);
						steps.add(HackingStep.BOOT9STRAP_FINAL_SETUP);
					}
					break;
				case 8:
					// TODO: Consider to implement DSiWare route
					break;
				default:
					// TODO: handle versions out of any range. Are they possible in first place?
					break;
			}
		} else {
			int range = -1;
			for(int i=0;i<N3DS_RANGES.length;i++) {
				FirmwareVersion from = N3DS_RANGES[i][0];
				FirmwareVersion to = N3DS_RANGES[i][1];
				if(this.fwver.gte(from) && this.fwver.lte(to)) {
					range = i;
					i = N3DS_RANGES.length;
				}
			}
			if(ConsoleRegion.JPN.equals(this.getFwver().getRegion()) && range == 0) {
				// TODO: Should we consider the Cubic Ninja option?
				steps.add(HackingStep.REQUIRES_UPDATE);
			} else if(range == 1) {
				steps.add(HackingStep.HOMEBREW_SOUNDHAX);
				steps.add(HackingStep.BOOT9STRAP_HOMEBREW);
				steps.add(HackingStep.BOOT9STRAP_FINAL_SETUP);
			} else if(range < 0) {
				// TODO: handle versions out of any range. Are they possible in first place?
			} else {
				steps.add(HackingStep.REQUIRES_UPDATE);
			}
		}
	}

	public List<HackingResource> resolveResources(String hackingDir) {
		// Use set to avoid duplicates
		Set<HackingResource> resourceSet = new HashSet<HackingResource>();
		this.hackingDir = hackingDir;
		for(HackingStep step:steps) {
			HackingProcess proc = HackingProcess.getInstance(step, hackingDir, sdCardDir);
			if(proc == null) {
				continue;
			}
			List<HackingResource> res = proc.getRequiredResources();
			if(res != null) {
				resourceSet.addAll(res);
			}
		}	
		return new ArrayList<HackingResource>(resourceSet);
	}

	public boolean requiresUpdate() {
		return steps.size() == 1 && HackingStep.REQUIRES_UPDATE.equals(steps.get(0));
	}
	
	public HackingStep getCurrentStep() {
		if(currentStepIndex < steps.size()) {
			return steps.get(currentStepIndex);
		}
		return null;
	}
	
	public boolean hasNext() {
		return currentStepIndex + 1 <= steps.size() - 1;
	}
	
	public void proceedToNext() {
		this.currentProcess = null;
		this.currentStepIndex++;
	}
	
	public HackingProcess getProcess()  {
		if(currentProcess == null) {
			currentProcess = HackingProcess.getInstance(getCurrentStep(), hackingDir, sdCardDir);
		}		
		return currentProcess;
	}

	public boolean isBrowserUsable() {
		boolean usablebrowser = false;
		if(this.fwver.getBrowser() > 25) {
			usablebrowser = true;
		} else if(this.fwver.getBrowser() > 0) {
			if(this.wasCartUpdated()) {
				if(this.fwver.lt("9.9.0")) {
					usablebrowser = true;
				}
			} else {
				usablebrowser = true;
			}
		}
		return usablebrowser;
	}
	
	public boolean isHackable() {
		return this.fwver.lte(FirmwareVersion.LATEST_HACKABLE);
	}

	public FirmwareVersion getFwver() {
		return fwver;
	}

	public void setFwver(FirmwareVersion fwver) {
		this.fwver = fwver;
	}

	public boolean wasCartUpdated() {
		return cartUpdated;
	}

	public void setCartUpdated(boolean cartUpdate) {
		this.cartUpdated = cartUpdate;
	}

	public String getHackingDir() {
		return hackingDir;
	}

	public void setHackingDir(String hackingDir) {
		this.hackingDir = hackingDir;
	}

	public String getSdCardDir() {
		return sdCardDir;
	}

	public void setSdCardDir(String sdCardDir) {
		this.sdCardDir = sdCardDir;
	}

}
