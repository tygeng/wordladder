#include"setbuilder.h"
#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include"vector.h"
#include"hashset.h"
#include"node.h"
// The program first build a graph of all word nodes, then use the graph
// to find all the shortest path between a source and a destination.
// The hashset is used to find a node given a string.
Hashset hindex;
// The vector also holds all the pointers to all the nodes, it's used to 
// make traversing the nodes faster when building the graph.
Vector dic;
// This set is used to build ladders from a source word to a destination 
// word. It holds different kind of nodes. Unlike the nodes stored in 
// hindex and dic, which are persistent through the life of the program.
// The nodes stored in cleanList only live during all the ladders from 
// a source word to a destination word are built.
// Specifically, the relatedWords in these node is only a subset of all 
// the possible related words. To understand what these nodes actually
// are, one may need to think about how you will find all the shortest
// ladders from given source word and destination word.
// The algorithm used here is elaborated later.
Hashset cleanList;
void printLadders(char* src, char* dst);
void printLaddersHelper(Node* src, Vector* ladder);
void buildNet(Vector* ref, Vector* wrt, Node** src);

int main(int argc, char** argv) {
    printf("Welcome to use Word Ladder Finder. Program starting...\n");
	// check the number of args
    if(argc!=2) {
        printf("Usage: wordladder <dictionary.txt>.\n");
        exit(0);
    }
    v_initialize(&dic, 128);
    h_initialize(&hindex);
	// build the graph, details about this operation is in setbuilder.c.
    build(argv[1], &hindex, &dic);
	// declare the buffer for the command, source word, and destination word.
    char command[13], src[30], dst[30];
    while (1) {
        scanf("%s", command);
		// print ladders
        if(strcmp(command,"ladder")==0) {
            scanf("%s", src);
            scanf("%s", dst);
            printLadders(src, dst);
        }
		// exit program
        else if(strcmp(command,"exit_program")==0)
            break;
		// find all the related words given a source word
        else if(strcmp(command,"relate")==0) {
            scanf("%s", src);
            Node node;
            nd_initialize(&node, src);
            if(h_find(&hindex, &node)) {
                Vector* list = &h_find(&hindex, &node)->relatedWords;
                if(list->size==0) {
                    printf("No related word found.\n");
                } else {
                    v_display(list);
                }
            }
            else {
                printf("Word \"%s\" doesn't exist.\n",src);
            }
            nd_cleanUp(&node);
        }

        else {
            printf("Usage:\tladder <source word> <destination word>\n\texit_program\n");
        }
    }


    h_cleanUp(&hindex);
    v_deepCleanUp(&dic);
	exit(0);
}

// The algorithm, put into words, is to grow a net around the destination
// word. And we will call collection of nodes that connects to nodes outside
// the current net border. Each time we grow the net, we trace through the
// border and add the new nodes that connect to the border node to a
// temporary list. Note this temporary list is the next border when we have
// finished growing the net from the previous border.
// When the net is grown, each new node is related to all the nodes in the
// previous border that can connect to it.
void printLadders(char* src, char* dst) {
	// each time printLadders is called, the cleanList is re-initialized.
    h_initialize(&cleanList);
	// these two lists are used to keep track of the current border and new border
	// when the net around the destination word is growing.
    Vector wrt, ref;
    v_initialize(&ref, 8);
    v_initialize(&wrt, 8);
    Node* srcNd,* dstNd;
	// create the source and destination node on the heap. They will
	// be added to the cleanList and the cleanList will clean up all the
	// nodes stored in it at the end.
    srcNd = malloc(sizeof(Node));
    dstNd = malloc(sizeof(Node));
    nd_initialize(srcNd, src);
    nd_initialize(dstNd, dst);
	// make sure the source word and destination word is in the dictionary.
    if(!h_find(&hindex, srcNd)) {
        printf("Word \"%s\" doesn't exist in the dictionary.\n", src);
        return;
    }
    if(!h_find(&hindex, dstNd)) {
        printf("Word \"%s\" doesn't exist in the dictionary.\n",dst);
        return;

    }
	// since we are going to grow the net from the destination word, we will
	// first add the destination to the cleanList and the reference list.
    h_add(&cleanList, dstNd);
    v_add(&ref, dstNd);
	// build the net
    buildNet(&ref,&wrt, &srcNd);
	
    v_cleanUp(&wrt);
    v_cleanUp(&ref);
    //start to print the ladders
	//the wrt list is reused as the buffer to store the current ladder when
	//printing all the ladders. So it's cleanedUp and re-initialized.
    v_initialize(&wrt,8);
    printLaddersHelper(srcNd, &wrt);
    v_cleanUp(&wrt);
	// do the deep clean up for all the nodes created for finding the ladders.
    h_deepCleanUp(&cleanList);
}
// print all the ladders recursively once the graph has been built.
void printLaddersHelper(Node* src, Vector* ladder) {
	// print the ladder if the buffer is filled with all the nodes up to the
	// destination
    if(src->relatedWords.size==0) {
        if (ladder->size==0) {
            printf("No ladder found.\n");
            return;
        }
        v_add(ladder, src);
        v_display(ladder);
        ladder->size--;
        return;
    }
    int i;
    for(i=0; i<src->relatedWords.size; i++) {
        v_add(ladder, src);
        printLaddersHelper(src->relatedWords.head[i], ladder);
        ladder->size--;
    }
}
void buildNet(Vector* ref, Vector* wrt, Node** src) {
	// to make checking duplicates on a border faster, we use a hashset.
    Hashset border;
    h_initialize(&border);
    int i;
	// traverse the current border
    for(i=0; i<ref->size; i++) {
        Node* current = h_find(&hindex, ref->head[i]);
        int j;
		// traverse the related words in a node on the border. If the word
		// is new (e.g not in cleanList nor the border), add the node on
		// the border to the new node's relatedWords list.
        for(j=0; j<current->relatedWords.size; j++) {
            if(h_find(&cleanList, current->relatedWords.head[j])) {
                continue;
            }
            Node* newNode = malloc(sizeof (Node));
            nd_initialize(newNode, current->relatedWords.head[j]->word);
            Node* nodeOnEdge;
            if((nodeOnEdge=h_find(&border, newNode))!=NULL) {
                nd_cleanUp(newNode);
                newNode=nodeOnEdge;
                nd_relate(newNode, ref->head[i]);
            } else {
                nd_relate(newNode, ref->head[i]);
                h_add(&border, newNode);
                v_add(wrt, newNode);
            }

        }
    }
	// sort the words on the new border alphabetically so the printed
	// ladders will be in the alphabetical order
    int k;
    for(k=0; k<wrt->size; k++) {
        qsort(wrt->head[k]->relatedWords.head,wrt->head[k]->relatedWords.size, sizeof(Node*),nd_compare);
    }

    v_cleanUp(ref);
    v_initialize(ref,8);
	// put all the new words in the cleanList, so they can be cleaned up
	// at the end.
    h_addAll(&cleanList, &border);
	// if the source word is on the new border, we are done finding a net
	// containg all the shortest ladders.
    Node* temp;
    if((temp=h_find(&border, *src))!=NULL) {
        nd_cleanUp(*src);
        *src=temp;
        h_cleanUp(&border);
        return;
    }
	// if we are not done, grow the net recursively
    h_cleanUp(&border);
    if(wrt->size) {
        buildNet(wrt,ref, src);
    }
}
