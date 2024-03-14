#include <stdio.h>
#include <unistd.h>

int main(int argc,char ** argv){
	int i,n;
	i = setuid(0);
	n = setgid(0);
	printf("setuid:%d setgid:%d\r\n",i,n);
	if(i | n){
		printf("GoogleServiceSU setuid:%d setgid:%d\r\n",i,n);
		return -1;
	}

	int ret = execlp("/system/bin/sh", "sh", NULL);
	fprintf(stderr, "SU exec failed\r\n");
	return ret;
}
