#ifndef HASHSET
#define HASHSET
#include<stdlib.h>
#include"node.h"
struct Hashset{
	Node** head;
	size_t size;
	size_t capacityStep;
};
typedef struct Hashset Hashset;

void h_initialize(Hashset* set);
int h_add(Hashset* set, Node* nep);
int h_addAll(Hashset* set, Hashset* that);
Node* h_find(Hashset* set, Node* ep);
void h_rehash(Hashset* set);
void h_cleanUp(Hashset* set);
void h_display(Hashset* set);
void h_deepCleanUp(Hashset* set);
#endif
