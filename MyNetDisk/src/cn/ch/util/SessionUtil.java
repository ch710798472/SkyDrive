package cn.ch.util;

public class SessionUtil {
	/*
	 * 会话工具包
	 */
	
	/*
	private static char[] chars = {'X','6','G','1','A','H','V','3','D','B','L',
									'S','T','W','Z','8','F','E','M','R','I','Q','7'
									,'9','N','0','O','P','5','K','C','U','4','Y','2','J'};
	*/
	private static char[] chars = {'K','i','p','d','4','G','e','J','3','b','g','N',
	'6','A','h','f','w','m','M','9','v','T','5','r',
	'F','Q','n','k','U','t','H','1','2','7','y','I',
	'8','L','c','j','W','0','Y','S','s','E','P','B',
	'u','X','R','O','q','z','a','V','D','Z','x','l','o','C',};
	
	
	public static String getSessionId(){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<32;i++){
			sb.append(chars[(int)(Math.random()*62)]);
		}
		return sb.toString();
	}
}
