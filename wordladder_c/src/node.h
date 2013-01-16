#ifndef NODE
#define NODE
#include"vector.h"
struct Node {
	char* word;
	Vector relatedWords;
};

// typedef struct Node Node;
void nd_initialize(Node* node, char* word);
void nd_relate(Node* this, Node* that);

int nd_equals(Node* this, Node* that);

int nd_hashCode(Node* this);
void nd_cleanUp(Node* this);
int nd_compare(const void*, const void*);

#endif
