package de.packsolite.mynpc.file;

public class MyNpcConfig {

	private long idCount = 0;
	private boolean checkPlaceholder = false;
	private boolean usePermission = false;

	public MyNpcConfig(long idStart, boolean checkPlaceholderDefault) {
		this.idCount = idStart;
		this.checkPlaceholder = checkPlaceholderDefault;
	}

	public long getId() {
		return this.idCount;
	}

	public long countId() {
		return ++this.idCount;
	}

	public boolean getCheckPlaceholder() {
		return this.checkPlaceholder;
	}

	public void setCheckPlaceholder(boolean b) {
		this.checkPlaceholder = b;
	}

	public boolean isUsePermission() {
		return usePermission;
	}
}