
#include <jni.h>
#include <stdio.h>
#include <string>
#include <iostream>
#include <android/log.h>
#include <dirent.h>


using namespace std;


//string LOG_TAG = "RunningApps";

int WriteInfo(char * procfn,char * pid,char * info){
	FILE * fp = fopen(procfn,"rb");
	if(fp <= 0){
		return 0;
	}

	char buf[1024] = {0};
	int fs = fread(buf,1,1024,fp);
	fclose(fp);

	int len = 0;
	if(fs > 0){
		//len = sprintf(result,"{\"��������\":\"%s\",\"����ID\":\"%s\",\"UID\":\"\",\"LRU\":\"\",\"description\":\"\"}",buf,pid);
		len = sprintf(info,"{\"\xE7\xA8\x8B\xE5\xBA\x8F\xE5\x90\x8D\xE7\xA7\xB0\":\"%s\",\"\xE8\xBF\x9B\xE7\xA8\x8BID\":\"%s\",\"UID\":\"\",\"LRU\":\"\",\"description\":\"\"},",buf,pid);
	}

	return len;
}


int GetProcInfo(char * info,int *result)
{
	DIR *dir = 0;
	struct dirent *ptr = 0;
	char procpath[] = "/proc/";
	if ((dir=opendir(procpath)) == NULL)
	{
		//__android_log_print(ANDROID_LOG_ERROR,LOG_TAG.c_str(),"open dir error");
		printf("Open dir error\r\n");
		perror("reason:");
		return -1;
	}

	int cnt = 0;
	while ((ptr=readdir(dir)) != NULL)
	{
		if(ptr->d_type == 8){
			continue;
		}
		else if(ptr->d_type == 4)
		{
			if( strcmp(ptr->d_name,"..") == 0 || strcmp(ptr->d_name,".") == 0){
				continue;
			}else{
				char folder[1024] = {0};
				strcpy(folder,procpath);
				strcat(folder,ptr->d_name);
				strcat(folder,"/cmdline");

				int len = WriteInfo(folder,ptr->d_name,info);
				if(len > 0){
					cnt ++;
					info += len;
					*result += len;
				}
			}
		}
	}
	closedir(dir);

	return cnt;
}

extern "C" JNIEXPORT jint JNICALL Java_com_phone_data_PhoneRunningApps_procRunningApps(JNIEnv * env,jobject obj,
		jstring dstfn){

	jboolean iscopy = 1;
	char * dstfilename = (char*)env->GetStringUTFChars(dstfn,&iscopy);

	//w+ �򿪿ɶ�д�ļ������ļ��������ļ�������Ϊ�㣬�����ļ����ݻ���ʧ�����ļ��������������ļ�
	FILE * fp = fopen(dstfilename,"wb+");
	if(fp <= 0){
		return -1;
	}

	fseek(fp,0,SEEK_SET);

	fwrite("[",1,1,fp);

	char * info = new char[0x100000];
	int len = 0;
	int cnt = GetProcInfo(info,&len);
	if(cnt > 0){
		fwrite(info,1,len,fp);

		fseek(fp,len,SEEK_SET);
	}

	delete []info;

	fwrite("]",1,1,fp);

	fclose(fp);

	return 0;
}
