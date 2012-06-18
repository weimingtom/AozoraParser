package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * @see http://www.aozora.gr.jp/cards/000035/card1567.html
 * 
 * @author Administrator
 *
 */
public class AozoraParser {
	private final static int KANJI = 1;
	private final static int HIRAGANA = 2;
	private final static int KATAKANA = 3;
	private final static int RUBY_OPEN = 4;
	private final static int RUBY_CLOSE = 5;
	private final static int RUBY_START = 6;
	private final static int OTHER = 99;
	
	private int start;
	private int offset;
	private boolean ruby_start;
	private int current;
	
	private String text;
	private StringBuffer ostrbuf;
	
	private String curRB;
	private String curRT;
	private int curRBStartPos;
	
	public final static class RubyInfo {
		public String rb;
		public String rt;
		public int rbStartPos;
		
		public RubyInfo(String rb, String rt, int rbStartPos) {
			this.rb = rb;
			this.rt = rt;
			this.rbStartPos = rbStartPos;
		}
	}
	private ArrayList<RubyInfo> rubyInfoList = new ArrayList<RubyInfo>();
	
	public AozoraParser() {
		
	}
	
	public void open(String filename, String breakLine) {
		FileInputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = new FileInputStream(filename);
			reader = new InputStreamReader(istr, "shift-jis");
			rbuf = new BufferedReader(reader);
			String line;
			StringBuffer sb = new StringBuffer();
			while (null != (line = rbuf.readLine())) {
				sb.append(line);
				sb.append(breakLine);
			}
			this.text = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rbuf != null) {
				try {
					rbuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void openInputStream(InputStream istr, String breakLine) {
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			reader = new InputStreamReader(istr, "shift-jis");
			rbuf = new BufferedReader(reader);
			String line;
			StringBuffer sb = new StringBuffer();
			while (null != (line = rbuf.readLine())) {
				sb.append(line);
				sb.append(breakLine);
			}
			this.text = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rbuf != null) {
				try {
					rbuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @see http://www.chiark.greenend.org.uk/~pmaydell/misc/aozora_ruby.py
	 * @param ch
	 * @return
	 */
	public int lex(char ch) {
        if ((ch >= '\u4e00' && ch <= '\u9fbf') || (ch == '\u3005')) {
            return KANJI;
        } else if (ch >= '\u3040' && ch <= '\u309f') {
            return HIRAGANA;
        } else if (ch >= '\u30a0' && ch <= '\u30ff') {
            return KATAKANA;
        } else if (ch == '\u300a') {
            return RUBY_OPEN;
        } else if (ch == '\u300b') {
            return RUBY_CLOSE;
        } else if (ch == '\uff5c') {
            return RUBY_START;
        } else {
            return OTHER;
        }
	}
	
	private void writesection() {
        if (start < offset) {
        	this.ostrbuf.append(this.text.substring(start, offset));
        }
        this.start = this.offset;
	}
	
	private String getsection() {
        if (start < offset) {
        	return this.text.substring(start, offset);
        }
        return null;
	}
	
	public void parseHTML() {
		start = 0;
		offset = 0;
		ruby_start = false;
		current = OTHER;
		curRB = null;
		curRT = null;
		curRBStartPos = -1;
		this.ostrbuf = new StringBuffer();
		for (this.offset = 0; offset < text.length(); offset++) {
			int charclass = lex(text.charAt(offset));
			if (charclass == RUBY_START) {
                writesection();
                start = start + 1;
        		ruby_start = true;
        		this.ostrbuf.append("<ruby><rb>");
			} else if (charclass == RUBY_OPEN) {
				if (!ruby_start) {
                    /**
                     * ruby applies to last section of contiguous same-type chars
                     */
					this.ostrbuf.append("<ruby><rb>");
	            }
                writesection();
                start = start + 1;
				this.ostrbuf.append("</rb><rt>");
			} else if (charclass == RUBY_CLOSE) {
                writesection();
                start = start + 1;
                this.ostrbuf.append("</rt></ruby>");
                ruby_start = false;
			} else if (charclass != current) {
                /**
                 * start of a different kind of character string
                 */
                writesection();
                current = charclass;
			}
		}
		writesection();
	}
	
	public void parseRuby() {
		start = 0;
		offset = 0;
		ruby_start = false;
		current = OTHER;
		curRB = null;
		curRT = null;
		curRBStartPos = -1;
		this.ostrbuf = new StringBuffer();
		for (this.offset = 0; offset < text.length(); offset++) {
			int charclass = lex(text.charAt(offset));
			if (charclass == RUBY_START) {
                writesection();
                start = start + 1;
        		ruby_start = true;
        		//this.ostrbuf.append("<ruby><rb>");
			} else if (charclass == RUBY_OPEN) {
				if (!ruby_start) {
                    /**
                     * ruby applies to last section of contiguous same-type chars
                     */
					//this.ostrbuf.append("<ruby><rb>");
	            }
				String rb = getsection();
				this.curRB = rb;
				//FIXME:
				this.curRBStartPos = this.ostrbuf.length();
                writesection();
                start = start + 1;
				//this.ostrbuf.append("</rb><rt>");
			} else if (charclass == RUBY_CLOSE) {
                if (false) {
                	writesection();
                } else {
                	String rt = getsection();
                	this.curRT = rt;
                	if (this.curRT != null) {
                		rubyInfoList.add(new RubyInfo(this.curRB, this.curRT, this.curRBStartPos));
                	}
                	this.curRB = this.curRT = null;
                	this.curRBStartPos = -1;
                	this.start = this.offset;
                }
                start = start + 1;
                //this.ostrbuf.append("</rt></ruby>");
                ruby_start = false;
			} else if (charclass != current) {
                /**
                 * start of a different kind of character string
                 */
                writesection();
                current = charclass;
			}
		}
		writesection();
	}
	
	public String getOutputString() {
		return this.ostrbuf.toString();
	}
	
	public void print() {
		System.out.println(getOutputString());
	}
	
	public ArrayList<AozoraParser.RubyInfo> getRubyList() {
		return this.rubyInfoList;
	}
	
	public void printRubyList() {
		for (int i = 0; i < this.rubyInfoList.size(); i++) {
			RubyInfo item = this.rubyInfoList.get(i);
			System.out.println("rb = " + item.rb + ", rt = " + item.rt + ", rbStartPos = " + item.rbStartPos);
		}
	}
	
	public void writeHTML(String filename) {
		FileOutputStream ostr = null;
		OutputStreamWriter writer = null;
		BufferedWriter obuf = null;
		try {
			ostr = new FileOutputStream(filename);
			writer = new OutputStreamWriter(ostr, "utf-8");
			obuf = new BufferedWriter(writer);
			obuf.write("<html>\n");
			obuf.write("<head>\n");
			obuf.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
			obuf.write("</head>\n");
			obuf.write("<body>\n");
			obuf.write(getOutputString());
			obuf.write("</body>\n");
			obuf.write("</html>\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (obuf != null) {
				try {
					obuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (ostr != null) {
				try {
					ostr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void writeNoRuby(String filename) {
		FileOutputStream ostr = null;
		OutputStreamWriter writer = null;
		BufferedWriter obuf = null;
		try {
			ostr = new FileOutputStream(filename);
			writer = new OutputStreamWriter(ostr, "utf-8");
			obuf = new BufferedWriter(writer);
			obuf.write(getOutputString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (obuf != null) {
				try {
					obuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (ostr != null) {
				try {
					ostr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		AozoraParser parser = new AozoraParser();
		if (false) {
			parser.open("hashire_merosu.txt", "<br />\n");
			parser.parseHTML();
			parser.print();
			parser.writeHTML("hashire_merosu.txt.html");
		} else {
			parser.open("hashire_merosu.txt", "\n");
			parser.parseRuby();
			parser.print();
			parser.writeNoRuby("hashire_merosu.noruby.txt");
			parser.printRubyList();
		}
	}
}

