package com.hggc.cdss.Utils;
//import java.io.BufferedReader;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class DSATool {
	
//	public void test() {
//		System.out.print("无用的测试函数");
//	}

	
//读取文件内容的函数	
//	 public static String txt2String(File file){
//    StringBuilder result = new StringBuilder();
//    try{
//        BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
//        String s = null;
//        while((s = br.readLine())!=null){//使用readLine方法，一次读一行
//            result.append(System.lineSeparator()+s);
//        }
//        br.close();    
//    }catch(Exception e){
//        e.printStackTrace();
//    }
//    return result.toString();
//}
	
	
	public void key() {
		if ((new java.io.File("myprikey.txt")).exists() == false) {
			if (generatekey() == false) {
				System.out.println("生成密钥对失败");
				return; 
			}
			;
	}
	}

	public  void sign(String myinfo) {
		try {
			java.io.ObjectInputStream in = new java.io.ObjectInputStream(
					new java.io.FileInputStream("myprikey.txt"));
			PrivateKey myprikey = (PrivateKey) in.readObject();
			//	System.out.println("私钥 =" + myprikey);
			in.close();
			//	File file = new File("guidelines.json");
			//    String myinfo = txt2String(file);
			//    System.out.println("myinfo( 指南内容 )=" + myinfo);
			//	String myinfo = "这是我的信息"; // 要签名的信息
			// 用私钥对信息生成数字签名
			java.security.Signature signet = java.security.Signature.getInstance("DSA");
			signet.initSign(myprikey);
			signet.update(myinfo.getBytes());
			byte[] signed = signet.sign(); // 对信息的数字签名
			System.out.println("signed( 签名内容 )=" + byte2hex(signed));
			// 把信息和数字签名保存在一个文件中
			java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
					new java.io.FileOutputStream("mysign.txt"));
//			out.writeObject(myinfo);
			out.writeObject(signed);
			out.close();
			System.out.println("签名并生成文件成功");
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("签名并生成文件失败");
		}
	}

	public  boolean verify(String info){
		try {
			java.io.ObjectInputStream in = new java.io.ObjectInputStream(
					new java.io.FileInputStream("mypubkey.txt"));
			PublicKey pubkey = (PublicKey) in.readObject();
			//	System.out.println("公钥 =" + pubkey);
			in.close();
			//	System.out.println(pubkey.getFormat());
			in = new java.io.ObjectInputStream(new java.io.FileInputStream("mysign.txt"));
			
			//	String Get_info = (String) in.readObject();
			//	info = "这不是我的信息";
			byte[] signed = (byte[]) in.readObject();
			in.close();
			java.security.Signature signetcheck = java.security.Signature.getInstance("DSA");
			signetcheck.initVerify(pubkey);
			signetcheck.update(info.getBytes());
			if (signetcheck.verify(signed)) {
				//		System.out.println("info=" + info);
				System.out.println("签名正常");
				return true;
			} else {
				System.out.println("签名非正常");
				return false;
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static public boolean generatekey() {
		try {
			//	System.out.print("生成密钥中");
			java.security.KeyPairGenerator keygen =
					java.security.KeyPairGenerator.getInstance("DSA");
			keygen.initialize(512);
			KeyPair keys = keygen.genKeyPair();// 生成密钥组
			PublicKey pubkey = keys.getPublic();
			PrivateKey prikey = keys.getPrivate();
			java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
					new java.io.FileOutputStream("myprikey.txt"));
			out.writeObject(prikey);
			out.close();
			System.out.println("写入对象 prikeys ok");
			out = new java.io.ObjectOutputStream(
					new java.io.FileOutputStream("mypubkey.txt"));
			out.writeObject(pubkey);
			out.close();
			System.out.println("写入对象 pubkeys ok");
			System.out.println("生成密钥对成功");
			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("生成密钥对失败");
			return false;
		}
	}

	public String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
				stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
				if (stmp.length() == 1) hs = hs + "0" + stmp;
				else hs = hs + stmp;
				if (n < b.length - 1) hs = hs + ":";
		}
		return hs.toUpperCase();
	}
}


