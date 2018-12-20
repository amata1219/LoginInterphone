package amata1219.login.interphone;

public class Util {

	public static String replaceAll(final String s, final String regex, String replacement){
		StringBuilder replace = new StringBuilder();
		final int sl = s.length();
		final int rl = regex.length();
		replace.append(s);
		boolean m = false;
		for(int i = 0; i < sl; i++){
			int start = replace.indexOf(regex, i);
			if(start == -1){
				if(start == 0){
					return s;
				}
				return replace.toString();
			}
			replace = replace.replace(start, start + rl, replacement);
			m = true;
		}
		if(!m){
			return s;
		}else{
			return replace.toString();
		}
	}

}
