package cn.ch.client;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import cn.ch.util.FileType;

public class TreeNodeData{
	/*
	 * 网盘目录节点
	 */
	private String nodeType;
	private String nodeName;
	private String nodeSize;
	
	public TreeNodeData(String nodeType,String nodeName){
		this.nodeType = nodeType;
		this.nodeName = nodeName;
	}
	public String getNodeType() {
		return nodeType;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodeSize() {
		return nodeSize;
	}
	
	public String toString(){
		return nodeName;
	}
	
}
/**
 * 节点绘制器，显示图片
 */
@SuppressWarnings("serial")
class MyRenderer extends DefaultTreeCellRenderer{
	ImageIcon home = new ImageIcon("images/folder_image/home.gif");
	ImageIcon folder = new ImageIcon("images/folder_image/folder.gif");
	ImageIcon txt = new ImageIcon("images/folder_image/txt.gif");
	ImageIcon excel = new ImageIcon("images/folder_image/excel.gif");
	ImageIcon defaultImg = new ImageIcon("images/folder_image/default.gif");

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		TreeNodeData nodeData = (TreeNodeData)node.getUserObject();
		ImageIcon icon = null;
		if(nodeData.getNodeType().equals(FileType.HOME))
			icon = home;
		else if(nodeData.getNodeType().equals(FileType.FOLDER))
			icon = folder;
		else if(nodeData.getNodeType().equals(FileType.TXT))
			icon = txt;
		else if(nodeData.getNodeType().equals(FileType.EXCEL))
			icon = excel;
		else
			icon = defaultImg;
		this.setIcon(icon);
		
		return this;
	}
	
}
