
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include "public.h"



char __aeabi_unwind_cpp_pr0[0];




int main(int argc,char ** argv){
	int ret = -1;
	ret = moveAppIntoSystem();
	return ret;
}
