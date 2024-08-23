package jogl8.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import jogl8.control;

import java.awt.event.MouseAdapter;

public class interactives {
	
	//物体不具有惯性
	public class StandardMouse extends MouseAdapter{
		@Override
		public void mouseDragged(MouseEvent e) {
			Point coord = e.getLocationOnScreen();
			control.RealtimeLocation = coord;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			Point coord = e.getLocationOnScreen();
			control.StartLocation = coord;
			control.RealtimeLocation = coord;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			control.StartLocation = new Point(0,0);
			control.RealtimeLocation = new Point(0,0);
		}
	}
	
	
	//物体具有惯性，单击空白处以停止
	//如果拖动后立刻松手，可以播放动画
	public class AutoAnimationMouse extends MouseAdapter{
		@Override
		public void mouseDragged(MouseEvent e) {
			Point coord = e.getLocationOnScreen();
			control.RealtimeLocation = coord;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			Point coord = e.getLocationOnScreen();
			control.StartLocation = coord;
			control.RealtimeLocation = coord;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}
	
	public class keyboard{
		
	}
}