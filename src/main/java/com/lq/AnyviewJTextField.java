package com.lq;

import javax.swing.*;

public class AnyviewJTextField extends JTextField {
	public JComboBox getJBox() {
		return jBox;
	}

	public void setJBox(JComboBox cbInput) {
		this.jBox = cbInput;
	}

	JComboBox jBox;
	boolean adjusting;

	@Override
	public void requestFocus() {
		// TODO Auto-generated method stub
		super.requestFocus();
		System.out.println("requestFocus");

		this.setText("    ");
		this.setText("");
	}

	public boolean isAdjusting() {
		return adjusting;
	}

	public void setAdjusting(boolean adjusting) {
		this.adjusting = adjusting;
	}

	/**
	 * @param cbInput
	 * @param adjusting
	 */
	private void setAdjusting(JComboBox jBox, boolean adjusting) {
		jBox.putClientProperty("is_adjusting", adjusting);
	}
}
