package cn.ch.client;

public class MyFile {
	/*
	 * �ļ�����
	 */
	private String name;//�ļ���
	private String fileType;//�ļ�����
	private long fileSize;//�ļ���С
	private String modifyDate;//�޸�ʱ��
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	
}
