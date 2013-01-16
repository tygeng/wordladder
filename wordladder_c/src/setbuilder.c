#include"setbuilder.h"
#include<stdio.h>
#include<string.h>
#include<ctype.h>
#define NUM_SUB_DICS 5

int related(char* this, char* that);
void build(char* fileName, Hashset* index, Vector* dic);
int compareSmart(const void*, const void*);
int comparePos;
Node** binsearch(Node* key, Node** head, size_t size, int (*compare)(const void*, const void*));
int related(char* this, char* that) {
    int i;
    int diff=0;
    for(i=0; this[i]&&that[i]; i++) {
        if(this[i]!=that[i]) {
            diff++;
        }
    }
    if(this[i]!=that[i]) return 0;
    return diff==1;
}

// build the net.
void build(char* fileName, Hashset* index, Vector* dic) {
    char line[80];
    FILE* fp = fopen(fileName,"rt");
    if(fp==0) {
        printf("Cannot read the dictionary!\nProgram will terminate.\n");
        exit(0);
    }
    v_initialize(dic,128);
	// read from the dictionary, remove newline characters, and convert all words to lower case.
    while(fgets(line,80,fp) != NULL) {
        int i;
        for	(i=0; line[i]!='\0'; i++) {
            if(line[i]=='\n' || line[i]=='\r') {
                line[i]='\0';
            } else {
                line[i]=tolower(line[i]);
            }
        }
		// allocate the storage for this node. This node will be cleaned up at the end by
		// the call to deep clean up the vector holding all the nodes.
        Node* temp = malloc(sizeof(Node));
        nd_initialize(temp, line);
		// make sure no duplicate is added.
        if(h_add(index, temp)) {
            v_add(dic, temp);
        }
    }
    fclose(fp);
// slow method, very slow on large dictionary
//     int i,j;
//     for(i=0; i<dic->size; i++) {
// 		printf("Building relation for word \"%s\".\n", dic->head[i]->word);
//         for(j=0; j<dic->size; j++) {
//             if(related(dic->head[i]->word,dic->head[j]->word)) {
//             v_add(&(dic->head[i]->relatedWords),dic->head[j]);
//             }
//         }
// fast method, use sub dictionaries to accerlerate the procedure to build
// the map.
    int i, j, k;

    Vector subdics[NUM_SUB_DICS];
    for(i=0; i<NUM_SUB_DICS; i++) {
        v_initialize(&subdics[i],128);
    }
	// build the sub-dictionaries
    for(j=0; j<dic->size; j++) {
        int len = strlen(dic->head[j]->word);
        for(i=0; i<NUM_SUB_DICS; i++) {
            if(len>i) {
                v_add(&subdics[i],dic->head[j]);
            }
        }
    }
	// sort the sub-dictionaries with compareSmart. See documentation for
	// compareSmart for more information
    for(i=0; i<NUM_SUB_DICS-1; i++) {
        comparePos=i;
        qsort(subdics[i].head,subdics[i].size,sizeof(Node*),compareSmart);
    }
	// find all the related words given a word in the dictionary
    for(k=0; k<dic->size; k++) {
        printf("Building relation for word \"%s\".                                   \r", dic->head[k]->word);
		// put the related words in each sub-dictionary, except the last one.
        for(i=0; i<NUM_SUB_DICS-1; i++) {
            comparePos=i;
			
            Node start, end;
            nd_initialize(&start,dic->head[k]->word);
            nd_initialize(&end, dic->head[k]->word);
            start.word[comparePos]='a';
            end.word[comparePos]='z'+1;
            Node** startp = binsearch(&start, subdics[i].head, subdics[i].size, compareSmart);
            Node** endp = binsearch(&end, subdics[i].head, subdics[i].size, compareSmart);
            endp++;
            Node** np;
            int len = strlen(dic->head[k]->word);
            for(np=startp; np<endp; np++) {
                if(len == strlen((*np)->word) && (*np)->word[i]!=dic->head[k]->word[i] && related((*np)->word, dic->head[k]->word)  )
                    v_add(&(dic->head[k]->relatedWords), *np);
            }
            nd_cleanUp(&start);
            nd_cleanUp(&end);
        }
		// now it works on the last dictioanry, which is different from the rest.
		// This one is not sorted using compareSmart, instead, it's sorted
		// alphabetically. This dictionary is traversed using the slow nethod. But
		// since it only contains words of lenth NUM_SUB_DICS or longer, the
		// traversion won't be too long.
        Node start, end;
        nd_initialize(&start, dic->head[k]->word);
        nd_initialize(&end, dic->head[k]->word);
        start.word[NUM_SUB_DICS-1]='a';
        start.word[NUM_SUB_DICS]='\0';
        end.word[NUM_SUB_DICS-1]='z'+ 1;
        Node** startp = binsearch(&start, subdics[i].head, subdics[i].size, nd_compare);
        Node** endp = binsearch(&end, subdics[i].head, subdics[i].size, nd_compare);
        Node** np;
        for(np=startp; np<=endp; np++) {
			int h;
			int FirstFewLettersSame = 1;
			for(h=0;h<NUM_SUB_DICS-1;h++) {
				if(dic->head[k]->word[h]!=(*np)->word[h]) {
					FirstFewLettersSame = 0;
					break;
				}
				
			}
            if(FirstFewLettersSame && related(dic->head[k]->word, (*np)->word)) {
                nd_relate(dic->head[k], *np);
            }

        }
        nd_cleanUp(&start);
        nd_cleanUp(&end);
    }
    printf("\n");
}

// A binary search which will return a position no matter whether the key
// is found or not. If the key is not found, the position right after where
// it should be will be returned
Node** binsearch(Node* key, Node** head, size_t size, int (*compare)(const void*, const void*)) {
    int compareResult = compare(&key, &head[size/2]);
    if(size==1) {
        return head;
    }
    if(compareResult>0) {
        return binsearch(key, &head[size/2],(size+1)/2, compare);
    } else if(compareResult<0) {
        return binsearch(key, head, size/2,compare);
    } else {
        return &head[size/2];
    }
}
// compare two words with the letter at position comparePos with the least
// priority. Therefore, all words that has different comparePos-th letter
// is collected to a continuous sequence.
int compareSmart(const void* this, const void* that) {
    Node* thisnd = *(Node**) this;
    Node* thatnd = *(Node**) that;

    char thisc[32];
    char thatc[32];
    int i, j;
    for(i=0,j=0; thisnd->word[i]!='\0' && thatnd->word[i]!='\0'; i++) {
        if(i!=comparePos) {

            thisc[j]=thisnd->word[i];
            thatc[j]=thatnd->word[i];
            j++;
        }
    }
    if(thisnd->word[i]=='\0' && thatnd->word[i]=='\0') {
        thisc[j]=thisnd->word[comparePos];
        thatc[j]=thatnd->word[comparePos];
    } else {
        thisc[j]=thisnd->word[i];
        thatc[j]=thatnd->word[i];
    }
    thisc[i]='\0';
    thatc[i]='\0';
    return strcmp(thisc, thatc);

}
