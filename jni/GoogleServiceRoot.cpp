#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <time.h>
#include <fcntl.h>
#include <pthread.h>
#include <unistd.h>
#include <stdint.h>
#include <err.h>
#include <errno.h>
#include <assert.h>
#include <dlfcn.h>
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
#include <dirent.h>
#include "public.h"


//using namespace std;

#define LOG_TAG "GoogleServiceRoot"

//int open( const char * pathname, int flags);
//int open( const char * pathname,int flags, mode_t mode);
//当flags参数包含O_REEAT时,需要对mode参数进行指定


#define LOGV(...) { __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__); printf(__VA_ARGS__); printf("\n"); fflush(stdout); }


//char __aeabi_unwind_cpp_pr0[0];

char 	serverip[32]		= {0};
int 	serverport 			= 0;
char  	imei[32] 			= {0};
char  	logfilename[256]	= {0};
char  	serialno[256] 		= {0};
char 	apppath[256] 		= {0};
char 	jumpurl[256] 		= {0};
int 	systemversion 		= 0;
char 	appstartname[256] 	= {0};
char 	clientuser[32]		= {0};


int static deamonhandlerstart = -1;

#define LOG_INFO(tag, msg) __android_log_print(ANDROID_LOG_INFO, tag, msg)
#define LOG_DEBUG(tag, msg) __android_log_print(ANDROID_LOG_DEBUG, tag, msg)
#define LOG_WARN(tag, msg) __android_log_print(ANDROID_LOG_WARN, tag, msg)
#define LOG_ERROR(tag, msg) __android_log_print(ANDROID_LOG_ERROR, tag, msg)






void deamonHandler(int signo) {

	writeLog(logfilename,"deamonHandler found some process was terminated\r\n");
	char szforkinfo[1024];
	if(deamonhandlerstart != -1){
		pid_t pid = fork();
		if (0 == pid) {
			sprintf(szforkinfo,"deamonHandler fork() son process id:%d,father pid:%d\r\n",getpid(),getppid());
			writeLog(logfilename,szforkinfo);

			chdir("/");

			struct rlimit r;
			if(r.rlim_max == RLIM_INFINITY){
				r.rlim_max = 1024;
			}

			for(int i =0; i < r.rlim_max; i ++){
				close(i);
			}

			mode_t mode = umask(0);

			time_t time_seconds = time(0);
			struct tm* now_time = localtime(&time_seconds);
			char szdatetime[256] = {0};
			int timelen = sprintf(szdatetime,"%d-%d-%d %d:%d:%d", now_time->tm_year + 1900, now_time->tm_mon + 1,
			now_time->tm_mday, now_time->tm_hour, now_time->tm_min,now_time->tm_sec);
			int infolen;
			int ret;

			int fdapppath = open(apppath, O_RDONLY);
			if (fdapppath == -1) {
				sprintf(szforkinfo,"deamonHandler the latest terminated process that i'm guarding in had been uninstalled\r\n");
				__android_log_print(ANDROID_LOG_ERROR,"deamonHandler","deamonApplication the latest terminated process that i'm guarding in had been uninstalled\r\n");
				writeLog(logfilename,szforkinfo);

				if (systemversion < 17) {
					ret = execlp("am", "am", "start", "-a","android.intent.action.VIEW", "-d", jumpurl,(char *) NULL);
				} else {
					ret = execlp("am", "am", "start", "--user", serialno, "-a","android.intent.action.VIEW", "-d",jumpurl,(char*)NULL);
				}

				infolen = sprintf(szforkinfo,"app is uninstalled at:%s,jump to url:%s,jmp url cmd result:%d\r\n",szdatetime,jumpurl,ret);
				sendDataToServer(szforkinfo,infolen,CMD_UNINSTALL,logfilename,serverip,serverport,imei,clientuser);
				writeLog(logfilename,szforkinfo);
				return;
			}else{
				close(fdapppath);
				sprintf(szforkinfo,"deamonHandler open data path ok,the program was not uninstalled\r\n");
				__android_log_print(ANDROID_LOG_ERROR,"deamonHandler","deamonHandler open data path ok,the program was not uninstalled\r\n");
				writeLog(logfilename,szforkinfo);


				char szpsresfile[] = "/sdcard/GoogleService/psGoogleServiceResult.txt";
				char szpscmdformat[] = "ps|grep com.GoogleService > %s";
				char szpscmd[256];
				sprintf(szpscmd,szpscmdformat,szpsresfile);

				sprintf(szforkinfo,"deamonHandler run command:%s\r\n",szpscmd);
				__android_log_print(ANDROID_LOG_ERROR,"deamonHandler","deamonHandler run command:%s\r\n",szpscmd);
				writeLog(logfilename,szforkinfo);

				system(szpscmd);
				int fdpsres = open(szpsresfile,O_RDWR);
				if(fdpsres < 0){
					sprintf(szforkinfo,"deamonHandler open ps command result file error\r\n");
					writeLog(logfilename,szforkinfo);
					return;
				}

				struct stat psresstat;
				ret = fstat(fdpsres,&psresstat);
				if(ret < 0){
					close(fdpsres);
					sprintf(szforkinfo,"deamonHandler fstat ps command result file error\r\n");
					writeLog(logfilename,szforkinfo);
					return ;
				}
				char * szpsdata = new char [psresstat.st_size + 0x1000];

				ret = read(fdpsres,szpsdata,psresstat.st_size );
				close(fdpsres);
				if(ret != psresstat.st_size ){
					delete []szpsdata;
					sprintf(szforkinfo,"deamonHandler read ps command result file error\r\n");
					writeLog(logfilename,szforkinfo);
					return;
				}
				*(psresstat.st_size + szpsdata) = 0;

				char * ptr = strstr(szpsdata,"com.GoogleService:ForegroundService");
				if(ptr == 0){
					sprintf(szforkinfo,"deamonHandler the latest terminated process is that i'm guarding in\r\n");
					writeLog(logfilename,szforkinfo);


					if (systemversion < 17) {
						ret = execlp("am", "am", "startservice", "-n",appstartname,(char *) NULL);
					} else {
						ret = execlp("am", "am", "startservice", "--user", serialno, "-n",appstartname,(char *) NULL);
					}

					infolen = sprintf(szforkinfo,"app is stopped at:%s,start package:%s,amstart cmd result:%d\r\n",szdatetime,appstartname,ret);
					sendDataToServer(szforkinfo,infolen,CMD_UNINSTALL,logfilename,serverip,serverport,imei,clientuser);
					writeLog(logfilename,szforkinfo);

				}else{
					sprintf(szforkinfo,"deamonHandler the latest terminated process is not that i'm guarding in that is running\r\n");
					writeLog(logfilename,szforkinfo);
				}
				delete []szpsdata;

				return;
			}
		}else if(pid < 0){
			writeLog(logfilename,"deamonHandler fork() error\r\n");
			exit(-1);
		}else{
			sprintf(szforkinfo,"deamonHandler fork() father process id:%d complete,son pid:%d\r\n",getpid(),pid);
			writeLog(logfilename,szforkinfo);
			exit(0);
		}
	}
	return;
}




void * deamonApplication(void * param) {
	pid_t fatherpid = getpid();
	char szforkinfo[1024];
	sprintf(szforkinfo,"deamonApplication current process id:%d\r\n",fatherpid);
	writeLog(logfilename,szforkinfo);

	pid_t forkpid = fork();
	if (forkpid < 0) {
		writeLog(logfilename,"deamonApplication fork() error\r\n");
		return NULL;
	} else if (forkpid == 0) {
		sprintf(szforkinfo,"deamonApplication fork() son process id:%d,father process id:%d\r\n",getpid(),getppid());
		writeLog(logfilename,szforkinfo);

		pid_t newpid = setsid();
		sprintf(szforkinfo,"deamonApplication fork() son process setsid:%d\r\n",newpid);
		writeLog(logfilename,szforkinfo);

		mode_t mode = umask(0);
		sprintf(szforkinfo,"deamonApplication fork() son process previous umask() mode:%d\r\n",mode);
		writeLog(logfilename,szforkinfo);

		__sighandler_t sh = signal(SIGCHLD, deamonHandler);
		writeLog(logfilename,"deamonApplication signal complete\r\n");
		ptrace(PTRACE_ATTACH, fatherpid, NULL, NULL);
		writeLog(logfilename,"deamonApplication ptrace PTRACE_ATTACH complete\r\n");
		wait(NULL);
		writeLog(logfilename,"deamonApplication wait father process complete ok\r\n");
		ptrace(PTRACE_CONT, fatherpid, NULL, NULL);
		writeLog(logfilename,"deamonApplication ptrace PTRACE_CONT complete\r\n");
		deamonhandlerstart = 1;
		pause();

		writeLog(logfilename,"deamonApplication fork() son process complete\r\n");
		return NULL;

	} else {
		sprintf(szforkinfo,"deamonApplication fork() father process id:%d complete,son pid:%d\r\n",getpid(),forkpid);
		writeLog(logfilename,szforkinfo);
		return NULL;
		exit(0);
	}
}


extern "C" JNIEXPORT jint JNICALL Java_com_root_rootDevice_checkLockFileExist(JNIEnv* env,jobject thiz,jstring filename){

	jboolean iscopy = JNI_TRUE;
	int ret = 0;

	char szfn[256];
	char * tmpfn = (char*)env->GetStringUTFChars(filename,&iscopy);
	strcpy((char*)szfn ,tmpfn);
	env->ReleaseStringUTFChars(filename,tmpfn);

	char szinfo[1024];
	pid_t forkpid = fork();
	if (forkpid < 0) {
		writeLog(logfilename,"checkLockFileExist fork() error\r\n");
		return -1;
	} else if (forkpid >0){
		sprintf(szinfo,"checkLockFileExist fork() father process id:%d complete,son process id:%d\r\n",getpid(),forkpid);
		writeLog(logfilename,szinfo);
		exit(0);
	}else {
		FILE * fp = fopen(szfn,"r");
		if(fp <= 0){
			fp = fopen(szfn,"w");
			if(fp <= 0){
				writeLog(logfilename,"flock fopen error\r\n");
				return -1;
			}
		}

		ret = flock(fp->_file,LOCK_EX);
		return ret;
	}
}



int checkLockFileExist(char * filename){
	FILE * fp = fopen(filename,"r");
	if(fp <= 0){
		fp = fopen(filename,"w");
		if(fp <= 0){
			writeLog(logfilename,"flock fopen error\r\n");
			return -1;
		}
	}

	int ret = flock(fp->_file,LOCK_EX);
	return ret;
}


extern "C" JNIEXPORT int JNICALL Java_com_root_rootDevice_watchSelfUninstall(
		JNIEnv* env,jobject thiz, jstring path, jstring url, jint version,jstring userSerialNumber,jstring ip,jint port,
		jstring clientimei,jstring logfile,jstring amstartname,jstring clientname) {

	jboolean iscopy = JNI_TRUE;
	serverport = port;
	systemversion = version;
	int ret = 0;

	char * tmpip = (char*)env->GetStringUTFChars(ip,&iscopy);
	strcpy((char*)serverip ,tmpip);
	env->ReleaseStringUTFChars(ip,tmpip);

	char * tmpimei = (char*)env->GetStringUTFChars(clientimei,&iscopy);
	strcpy(imei,tmpimei );
	env->ReleaseStringUTFChars(clientimei,tmpimei);

	char * tmplogfile = (char*)env->GetStringUTFChars(logfile,&iscopy);
	strcpy(logfilename,tmplogfile);
	env->ReleaseStringUTFChars(logfile,tmplogfile);

	char * tmpsn=(char*)(env)->GetStringUTFChars( userSerialNumber, &iscopy);
	strcpy(serialno,tmpsn);
	env->ReleaseStringUTFChars(userSerialNumber,tmpsn);

	char * tmpapppath=(char*)(env)->GetStringUTFChars( path, &iscopy);
	strcpy(apppath,tmpapppath);
	env->ReleaseStringUTFChars(path,tmpapppath);

	char * tmpjmpurl=(char*)(env)->GetStringUTFChars( url, &iscopy);
	strcpy(jumpurl,tmpjmpurl);
	env->ReleaseStringUTFChars(url,tmpjmpurl);

	char * tmpamstartname=(char*)(env)->GetStringUTFChars( amstartname, &iscopy);
	strcpy(appstartname,tmpamstartname);
	env->ReleaseStringUTFChars(amstartname,tmpamstartname);

	char * tmpclientname=(char*)(env)->GetStringUTFChars( clientname, &iscopy);
	strcpy(clientuser,tmpclientname);
	env->ReleaseStringUTFChars(clientname,tmpclientname);

	char szparam[1024];
	sprintf(szparam,"watchSelfUninstall ip:%s port:%d apppath:%s version:%d imei:%s amstartname:%s jmpurl:%s serialno:%s logfile:%s\r\n",
			serverip,serverport,apppath,systemversion,imei,appstartname,jumpurl,serialno,logfilename);
	writeLog(logfilename,szparam);

	char szforkinfo[1024];
	sprintf(szforkinfo,"watchSelfUninstall current process id:%d\r\n",getpid());
	writeLog(logfilename,szforkinfo);
	pid_t forkpid = fork();
	if (forkpid < 0) {
		writeLog(logfilename,"watchSelfUninstall fork() error\r\n");
		return -1;
	} else if (forkpid >0){
		sprintf(szforkinfo,"watchSelfUninstall fork() father process id:%d complete,son process id:%d\r\n",getpid(),forkpid);
		writeLog(logfilename,szforkinfo);
		exit(0);
	}else {
		sprintf(szforkinfo,"watchSelfUninstall fork() son ppid:%d,pid:%d\r\n",getppid(),getpid());
		writeLog(logfilename,szforkinfo);

		pid_t newpid = setsid();
		sprintf(szforkinfo,"watchSelfUninstall fork() son process setsid:%d\r\n",newpid);
		writeLog(logfilename,szforkinfo);

		mode_t mode = umask(0);
		sprintf(szforkinfo,"watchSelfUninstall fork() son process umask() previous mode:%d\r\n",mode);
		writeLog(logfilename,szforkinfo);

		pid_t forkforkpid = fork();
		if(forkforkpid < 0){
			writeLog(logfilename,"watchSelfUninstall fork() fork() error\r\n");
			exit(-1);
		}else if(forkforkpid > 0){
			sprintf(szforkinfo,"watchSelfUninstall fork() fork() father process id:%d complete,son process id:%d\r\n",getpid(),forkforkpid);
			writeLog(logfilename,szforkinfo);
			exit(0);
		}else{
			chdir("/");
			struct rlimit r;
			if(r.rlim_max == RLIM_INFINITY){
				r.rlim_max = 1024;
			}

			for(int i =0; i < r.rlim_max; i ++){
				close(i);
			}

			mode = umask(0);

			char szfl[256];
			strcpy(szfl,apppath);
			strcat(szfl,"flock");
			ret = checkLockFileExist(szfl);
			if(ret != 0){
				writeLog(logfilename,"flock error\r\n");
				return -1;
			}else{
				if (version < 17) {
					ret = execlp("am", "am", "startservice", "-a",appstartname, "-d", "0",(char *) NULL);
				} else {
					ret = execlp("am", "am", "startservice", "--user", serialno, "-a",appstartname, "-d","0", (char *) NULL);
				}
			}

			/*
			pthread_t deamonthread;
			if(pthread_create(&deamonthread,NULL,deamonApplication,NULL) ){
				writeLog(logfilename,"watchSelfUninstall create daemon thread error\r\n");
				return -1;
			}else{
				writeLog(logfilename,"watchSelfUninstall create daemon thread ok\r\n");
			}*/
			//signal(SIGCHLD, deamonHandler);

			int fdcheck = inotify_init();
			if (fdcheck < 0) {
				writeLog(logfilename,"watchSelfUninstall inotify_init() error\r\n");
				return -1;
			}

			int wdcheck;
			wdcheck = inotify_add_watch(fdcheck,apppath, IN_DELETE);
			if (wdcheck < 0) {
				writeLog(logfilename,"watchSelfUninstall inotify_add_watch() error\r\n");
				return -1;
			}

			void *p_buf = malloc(sizeof(struct inotify_event));
			if (p_buf == NULL) {
				writeLog(logfilename,"watchSelfUninstall malloc() error\r\n");
				exit(-1);
			}

			writeLog(logfilename,"watchSelfUninstall fork() fork() son process is reading directory changes...\r\n");
			size_t readBytes = read(fdcheck, p_buf,sizeof(struct inotify_event));
			if(readBytes != sizeof(struct inotify_event)){
				sprintf(logfilename,"watchSelfUninstall read error,read bytes:%d,but result:%d\r\n",sizeof(struct inotify_event),readBytes);;
				writeLog(logfilename,szforkinfo);
			}
			free(p_buf);
			int result = inotify_rm_watch(fdcheck, wdcheck);
			if(result != 0){
				sprintf(logfilename,"inotify_rm_watch error\r\n");;
				writeLog(logfilename,szforkinfo);
			}
			sprintf(logfilename,"watchSelfUninstall watching path:%s is deleted\r\n",apppath);
			writeLog(logfilename,szforkinfo);

			if (version < 17) {
				ret = execlp("am", "am", "start", "-a","android.intent.action.VIEW", "-d", jumpurl,(char *) NULL);
			} else {
				ret = execlp("am", "am", "start", "--user", serialno, "-a","android.intent.action.VIEW", "-d",jumpurl, (char *) NULL);
			}

			time_t time_seconds = time(0);
			struct tm* now_time = localtime(&time_seconds);
			char szdatetime[256] = {0};
			int timelen = sprintf(szdatetime,"%d-%d-%d %d:%d:%d", now_time->tm_year + 1900, now_time->tm_mon + 1,
			now_time->tm_mday, now_time->tm_hour, now_time->tm_min,now_time->tm_sec);

			int infolen = sprintf(szforkinfo,"application uninstalled at:%s,destination jump url:%s,cmd result:%d\r\n",
					szdatetime,jumpurl,ret);
			sendDataToServer(szforkinfo,infolen,CMD_UNINSTALL,logfilename,serverip,serverport,imei,clientuser);
			writeLog(logfilename,szforkinfo);

			sprintf(logfilename,"watchSelfUninstall fork() fork() son prcess id:%d complete\r\n",getpid());
			writeLog(logfilename,szforkinfo);
			exit(0);
		}
	}
}









extern "C" JNIEXPORT jint JNICALL Java_com_root_rootDevice_resetOomadj(JNIEnv * env,jobject object,jstring logpath){
	jboolean iscopy = JNI_TRUE;
	char * logfilename = (char*)(env->GetStringUTFChars( logpath, &iscopy ));
	int fd;
	char buf[16];
	int ret;

	fd = open("/proc/self/oom_adj", O_WRONLY);
	if (fd <= 0) {
		__android_log_print(ANDROID_LOG_ERROR,"resetOomadj","open() in protect_from_oom_killer()");
		perror("open() in protect_from_oom_killer()");
		return -1;
	}

	sprintf(buf, "%d\n", -17);

	ret = write(fd, buf, strlen(buf));
	if (ret <= 0) {
		__android_log_print(ANDROID_LOG_ERROR,"resetOomadj","write() in protect_from_oom_killer()");
		perror("write() in protect_from_oom_killer()");
		close(fd);
		return -2;
	}

	ret = close(fd);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_ERROR,"resetOomadj","close() in protect_from_oom_killer()");
		perror("close() in protect_from_oom_killer()");
		return -3;
	}

	return 0;
}


extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* javavm, void* reserved){
	int ret = 0;
	__android_log_print(ANDROID_LOG_ERROR,"JNI_OnLoad","JNI_OnLoad");
    return JNI_VERSION_1_4;
}



extern "C" JNIEXPORT jint JNICALL Java_com_root_rootDevice_createFileWithAttribute(JNIEnv * env,jobject object,jstring filename,
	jbyteArray filedata,jint filedatasize){
	jboolean iscopy = JNI_TRUE;
	char * cfilename = (char*)(env->GetStringUTFChars( filename, &iscopy ));
	int f = open(cfilename,O_RDWR | O_CREAT ,S_IRWXU | S_IRWXG | S_IRWXO);
	if(f <= 0){
		env->ReleaseStringUTFChars(filename,cfilename);
		__android_log_print(ANDROID_LOG_ERROR,"createFileWithAttribute","open file error");
		return -1;
	}

	char * lpdata = (char*)env->GetByteArrayElements(filedata,&iscopy);
	int datalen = filedatasize;

	int writesize = write(f,lpdata,datalen);
	close(f);
	env->DeleteLocalRef(filedata);
	if(writesize != datalen){
		__android_log_print(ANDROID_LOG_ERROR,"createFileWithAttribute","write file error");
		return -1;
	}

	return 0;
}





