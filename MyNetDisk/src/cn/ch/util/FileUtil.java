package cn.ch.util;

import java.io.File;

import cn.ch.client.MyFile;

public class FileUtil {
	/*
	 * 文件目录
	 */
	public static File[] getFolder(String parentFolder){
		File file = new File(parentFolder);
		return file.listFiles();//获取文件夹
	}
	
	/*
	 * 文件类型
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
	 *文件名与文件类型 
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
