package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file. The root of the 
	 * tree is stored in the root field.
	 */
	public void build() {
		String line = "";
		Stack<TagNode> level = new Stack<TagNode>();
		while(sc.hasNextLine()) {
			line = sc.nextLine();
			if(line.charAt(0) == '<' && line.charAt(line.length() - 1) == '>') {
				if(line.charAt(1) == '/') {
					level.pop();
				}
				else {
					TagNode t = new TagNode(line.replace("<", "").replace(">", ""), null, null);
					if(root == null) root = t;
					else {
						TagNode p = level.peek();
						if(p.firstChild == null) p.firstChild = t;
						else {
							TagNode last = p.firstChild;
							while(last.sibling != null) {
								last = last.sibling;
							}
							last.sibling = t;
						}
					}
					level.push(t);
				}
			}
			else {
				TagNode t = new TagNode(line, null, null);
				TagNode p = level.peek();
				if(p.firstChild == null) p.firstChild = t;
				else {
					TagNode last = p.firstChild;
					while(last.sibling != null) {
						last = last.sibling;
					}
					last.sibling = t;
				}
			}
		}
	}
	
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		findAndReplace(oldTag, newTag, root);
	}
	
	private void findAndReplace(String oldTag, String newTag, TagNode root) {
		//String s = oldTag.toLowerCase();
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild != null) {
				findAndReplace(oldTag, newTag, ptr.firstChild);
				if(ptr.tag.equals(oldTag)) {
					ptr.tag = newTag;
				}
			}
		}
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		TagNode table = findTable(root);
		if(table != null) {
			TagNode trow = table.firstChild;
			for(int i = 1; i < row; i++) {
				trow = trow.sibling;
				if(trow == null) break;
			}
			if(trow != null) {
				for(TagNode ptr = trow.firstChild; ptr != null; ptr = ptr.sibling) {
					TagNode bold = new TagNode("b", ptr.firstChild, null);
					ptr.firstChild = bold;
				}
			}
			
		}
		
	}
	private TagNode findTable(TagNode root) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if(ptr.firstChild != null) {
				if(ptr.tag.equals("table")) return ptr;
				TagNode tab = findTable(ptr.firstChild);
				if(tab != null) return tab;
			}
		}
		return null;
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		findAndRemove(tag, root, null);
	}
	
	private void findAndRemove(String tag, TagNode root, TagNode parent) {
		TagNode prev = null;
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild != null) {
				findAndRemove(tag, ptr.firstChild, ptr);
				if((ptr.tag.equals("p") || ptr.tag.equals("em") || ptr.tag.equals("b")) && (ptr.tag.equals(tag))) {
					if(prev == null) {
						parent.firstChild = ptr.firstChild;
					}
					else {
						prev.sibling = ptr.firstChild;
					}
					TagNode last = ptr.firstChild;
					while(last.sibling != null) last = last.sibling;
					last.sibling = ptr.sibling;
					ptr = ptr.firstChild;
				}
				else if((ptr.tag.equals("ol") || ptr.tag.equals("ul")) && (ptr.tag.equals(tag))) {
					for (TagNode ptr2=ptr.firstChild; ptr2 != null;ptr2=ptr2.sibling) {
						if(ptr2.tag.equals("li")) {
							ptr2.tag = "p";
						}
					}
					if(prev == null) {
						parent.firstChild = ptr.firstChild;
					}
					else {
						prev.sibling = ptr.firstChild;
					}
					TagNode last = ptr.firstChild;
					while(last.sibling != null) last = last.sibling;
					last.sibling = ptr.sibling;
					ptr = ptr.firstChild;
				}
			}
			prev = ptr;
		}
	}
	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		findAndTag(word, tag, root, null);
	}
	private void findAndTag(String word, String tag, TagNode root, TagNode parent) {
		String small = word.toLowerCase();
		TagNode prev = null;
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild != null) {
				findAndTag(word, tag, ptr.firstChild, ptr);	
			}
			else {
				String s = ptr.tag;
				String[] split = s.split(" ");
				String recon = "";
				for(int i = 0; i < split.length; i++) {
					String inst = split[i].toLowerCase();
					if(inst.length() == small.length() || (inst.length() - 1) == small.length()) {
						if(small.equals(inst) || small.equals(inst.substring(0, inst.length() - 1))) {
							if(inst.length() > small.length()) {
								 char c = inst.charAt(inst.length() - 1);
								 if(!(c == '!' || c == '.' || c == ';' || c == ',' || c == ',' || c == '?')) {
								     recon += split[i] + (i + 1 == split.length ? "" : " ");
									 continue;
								 }
							}
							//int index = s.indexOf(recon);
							String fhalf = recon; //s.substring(0, index);
							String shalf = s.substring(recon.length() + split[i].length());
							//ecSystem.out.println(fhalf);
							TagNode firstHalf = fhalf.isEmpty() ? null : new TagNode(fhalf, null, null);
							TagNode secondHalf = shalf.isEmpty() ? null : new TagNode(shalf, null, null);
							TagNode newNode = new TagNode(tag, new TagNode(split[i], null, null), null);
							if(prev != null) {
								prev.sibling = newNode;
							}
							newNode.sibling = ptr.sibling;
							if(firstHalf != null) {
								if(prev!= null) {
									prev.sibling = firstHalf;
								}
								firstHalf.sibling = newNode;
							}
							if(secondHalf != null) {
								newNode.sibling = secondHalf;
								secondHalf.sibling = ptr.sibling;								
							}
							if(parent != null && parent.firstChild == ptr) {
								if(firstHalf != null) {
									parent.firstChild = firstHalf;
								}
								else {
									parent.firstChild = newNode;
								}
							}
							ptr = newNode;
							break;
						}
					}
					recon += split[i] + (i + 1 == split.length ? "" : " ");
				}
			}
			prev = ptr;
		}
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
}
