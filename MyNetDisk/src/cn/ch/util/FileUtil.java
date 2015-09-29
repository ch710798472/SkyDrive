package cn.ch.util;

import java.io.File;

import cn.ch.client.MyFile;

public class FileUtil {
	/*
	 * �ļ�Ŀ¼
	 */
	public static File[] getFolder(String parentFolder){
		File file = new File(parentFolder);
		return file.listFiles();//��ȡ�ļ���
	}
	
	/*
	 * �ļ�����
	 */
	public static String getFileType(File file){
		String name = file.getName().toUpperCase();
		if(file.isDirectory())
			return FileType.FOLDER;
		else if(name.endsWith(FileType.TXT))
			return FileType.TXT;
		else if(name.toUpperCase().endsWith(FileType.WORD2003)||name.endsWith(FileType.WORD2007))
			return FileType.WORD;
		else if(name.toUpperCase().endsWith(FileType.EXCEL2003)||name.endsWith(FileType.EXCEL2007))
			return FileType.EXCEL;
		else 
			return FileType.DEFAULT;
	}
	
	/*
	 *�ļ������ļ����� 
	 */
	public static MyFile getMyFile(String lineData){
		String[] s = lineData.split(SessionProtocol.FILE_SPLIT_FILETYPE);
		if(s.length==2){
			MyFile file = new MyFile();
			file.setName(s[0]);
			file.setFileType(s[1]);
			return file;
		}
		return null;
	}
	
}
