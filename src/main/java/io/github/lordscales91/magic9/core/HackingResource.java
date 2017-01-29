package io.github.lordscales91.magic9.core;

import io.github.lordscales91.magic9.DownloadWorker;
import io.github.lordscales91.magic9.MagicWorker;

import java.io.File;

public class HackingResource {
	private String url;
	private File out;
	public HackingResource(String url, File out) {
		this.url = url;
		this.out = out;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public File getOut() {
		return out;
	}
	public void setOut(File out) {
		this.out = out;
	}
	public MagicWorker getWorker(String tag, CallbackReceiver receiver) {
		return new DownloadWorker(url, out, tag, receiver);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((out == null) ? 0 : out.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HackingResource other = (HackingResource) obj;
		if (out == null) {
			if (other.out != null)
				return false;
		} else if (!out.equals(other.out))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return String.format("<HackingResource url: %s out: %s>", url, out.getName());
	}
	
	
}
