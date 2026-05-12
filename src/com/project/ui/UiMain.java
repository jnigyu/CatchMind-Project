package com.project.ui;

import javax.swing.JFrame;

public class UiMain {
    public static void main(String[] args) {
        // 1. 도화지 역할을 할 창(Frame) 객체 생성
        JFrame frame = new JFrame("캐치마인드 프로젝트"); 
        
        // 2. 창의 크기 설정 (가로 800픽셀, 세로 600픽셀)
        frame.setSize(800, 600); 
        
        // 3. 우측 상단 X 버튼을 누르면 자바 프로그램도 완전히 종료되도록 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
        // 4. 세팅이 끝난 창을 화면에 보여주기
        frame.setVisible(true); 
    }
}