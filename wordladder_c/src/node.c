#include"node.h"
#include<string.h>
void nd_initialize(Node* node, char* word) {
	node->word = malloc((strlen(word)+1)*sizeof(char));
	strcpy(node->word,word);
	v_initialize(&node->relatedWords, 10);
}
void nd_relate(Node* this, Node* that) {
	v_add(&this->relatedWords, that);
}

int nd_equals(Node* this, Node* that) {
	return !strcmp(this->word, that->word);
}

int nd_hashCode(Node* this) {
	char* word  = this->word;
	int hashValue = 0;
	int pos;
	for(pos = 0; word[pos]!='\0';pos++) {
		hashValue = (hashValue<<4)+word[pos];
		int hibit = hashValue & 0xF0000000;
		if(hibit) {
			hashValue ^= hibit>>24;
		}
		hashValue &= ~hibit;
	}
	return hashValue;
}
void nd_cleanUp(Node* this) {
	free(this->word);
	v_cleanUp(&this->relatedWords);
}
int nd_compare(const void* this, const void* that) {
	Node** thisnd = (Node**) this;
	Node** thatnd = (Node**) that;
	return strcmp((*thisnd)->word, (*thatnd)->word);
}
