package com.utils;

public class CryptData {
	
	public static String gKey = "fuck all android crackers";
	
	public static void encrypt(byte[] src,byte[]dst,byte[] key,int srcpos,int dstpos,int len){

		int keylen = key.length;

		for (int i = 0,j = 0; i < len; i++) {
			
			dst[i+dstpos] = (byte)(src[i+srcpos] ^ key[j]);
			j ++;
			if(j >= keylen ){
				j = 0;
			}
		}

		return ;
	}

    public static byte[] xorCryptData(byte[]data,byte[]key){
    	
    	if(key.length <= 0 || data.length <= 0){
    		return data;
    	}
    	
    	for(int i = 0,j = 0; i < data.length; i ++){
    		data[i] ^= key[j];
    		j ++;
    		if(j >= key.length){
    			j = 0 ;
    		}
    	}
    	
    	return data;
    }
    
}
