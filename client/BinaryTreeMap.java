/**
 * Created by billzhang on 2017-05-30.
 */

public class BinaryTreeMap {

    private String key;
    private String val;
    private BinaryTreeMap next;
    private BinaryTreeMap prev;

    public BinaryTreeMap(String key, String val){
        this.key = key;
        this.val = val;
        next = null;
        prev = null;
    }

    public String getKey(){
        return key;
    }

    public String getVal(){
        return val;
    }

    public BinaryTreeMap getNext(){
        return next;
    }

    public BinaryTreeMap getPrev(){
        return prev;
    }

    public void setKey(String key){
        this.key = key;
    }

    public void setVal(String val){
        this.val = val;
    }

    public void setNext(BinaryTreeMap next){
        this.next = next;
    }

    public void setPrev(BinaryTreeMap prev){
        this.prev = prev;
    }

    public static void insert(BinaryTreeMap nodeToBeInserted, BinaryTreeMap rootNode){

        // result stores the result of the comparison between key and rootNode
        int result = nodeToBeInserted.getKey().compareToIgnoreCase(rootNode.getKey());

        // if key of the node to be inserted comes before root node...
        if (result < 0){
            if (rootNode.getPrev() != null){
                insert(nodeToBeInserted, rootNode.getPrev());
            }else{
                rootNode.setPrev(nodeToBeInserted);
			}
        }

        // if key of the node to be inserted comes after root node...
        else if (result > 0){
            if (rootNode.getNext() != null){
                insert(nodeToBeInserted, rootNode.getNext());
            }else{
                rootNode.setNext(nodeToBeInserted);
			}
        }

        
    }

   public static BinaryTreeMap search(String key, BinaryTreeMap rootNode){

        // result stores the result of the comparison between key and rootNode
        int result = key.compareToIgnoreCase(rootNode.getKey());

        //if found...
        if (result == 0){
            return rootNode;
		}

        //if the key comes before that of rootNode...
        else if (result < 0){
            if (rootNode.getPrev() == null){
                return null;
            }else{
                return search(key, rootNode.getPrev());
			}
        }

        //if the key comes after that of rootNode...
        else {
            if (rootNode.getNext() == null){
                return null;
            }else{
                return search(key, rootNode.getNext());
        	}
		}
    }

}
