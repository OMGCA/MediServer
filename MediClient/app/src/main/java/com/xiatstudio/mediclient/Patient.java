package com.xiatstudio.mediclient;

import java.io.Serializable;

public class Patient implements Serializable {
    /* 个人信息 */
    String name; //姓名
    int age; //年龄
    String sex; //性别
    String slotID; //床位（或NFC标签标识）
    String doctor;//主治医师
    String careLevel;
    String careTaboo;

    /* 数据信息 */
    double bodyTemp; //体温，℃
    int pulse; //脉搏，次/min
    int breath; //呼吸，次/min
    int releasePressure; //血压舒张压
    int tensePressure; //血压收缩压, mmHg
    double bgAnalysis; //血气分析, pH
    int bloodNa; //血气分析-血纳
    double bloodK; //血气分析-血钾, mmol/L

    /* 文字描述 */
    String opPending; //手术类型
    String pathologyResult; //病理报告
    String imagingResult; //影像学结果
    String ECG;//心电图
    String docNote; //医师嘱咐


    //Patient类初始化
    public Patient(String name) {
        this.name = name;
        this.age = 0;
        this.sex = "";
        this.slotID = "";
        this.doctor = "";
        this.careLevel = "暂无等级信息";
        this.careTaboo = "暂无禁忌信息";

        this.bodyTemp = 0;
        this.pulse = 0;
        this.breath = 0;
        this.releasePressure = 0;
        this.tensePressure = 0;
        this.bgAnalysis = 0;
        this.bloodNa = 0;
        this.bloodK = 0;

        this.opPending = "暂无手术安排";
        this.pathologyResult = "暂无病理分析";
        this.imagingResult = "暂无影像学结果";
        this.ECG = "暂无心电图结果";
        this.docNote = "暂无医嘱";
    }

    //姓名
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    //年龄
    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return this.age;
    }

    //性别
    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSex(){
        return this.sex;
    }

    //床位
    public void setSlotID(String slotID){
        this.slotID = slotID;
    }

    public String getSlotID(){
        return this.slotID;
    }

    public void setDoc(String doc) {
        this.doctor = doc;
    }

    public String getDoc() {
        return this.doctor;
    }

    public void setLevel(String level) {
        this.careLevel = level;
    }

    public String getLevel() {
        return this.careLevel;
    }

    public void setTaboo(String taboo) {
        this.careTaboo = taboo;
    }

    public String getTaboo() {
        return this.careTaboo;
    }

    public void setBodyTemp(double temp){
        this.bodyTemp = temp;
    }

    public double getBodyTemp(){
        return this.bodyTemp;
    }

    public void setPulse(int pulse){
        this.pulse = pulse;
    }

    public int getPulse(){
        return this.pulse;
    }

    public void setBreath(int breath){
        this.breath = breath;
    }

    public int getBreath(){
        return this.breath;
    }

    public void setReleasePressure(int rPressure){
        this.releasePressure = rPressure;
    }

    public int getReleasePressure(){
        return this.releasePressure;
    }

    public void setTensePressure(int tPressure){
        this.tensePressure = tPressure;
    }

    public int getTensePressure(){
        return this.tensePressure;
    }

    public void setBgAnalysis(double bg){
        this.bgAnalysis = bg;
    }

    public double getBgAnalysis(){
        return this.bgAnalysis;
    }

    public void setBloodNa(int bNa){
        this.bloodNa = bNa;
    }

    public int getBloodNa(){
        return this.bloodNa;
    }

    public void setBloodK(double bK){
        this.bloodK = bK;
    }

    public double getBloodK(){
        return this.bloodK;
    }

    public void setOpPending(String newOp){
        this.opPending = newOp;
    }

    public String getOpPending(){
        return this.opPending;
    }

    public void setPathologyResult(String newPath){
        this.pathologyResult = newPath;
    }

    public String getPathologyResult(){
        return this.pathologyResult;
    }

    public void setImaging(String newImage){
        this.imagingResult = newImage;
    }

    public String getImaging(){
        return this.imagingResult;
    }

    public void setECG(String ECG) {
        this.ECG = ECG;
    }

    public String getECG() {
        return this.ECG;
    }

    public void setDocNote(String newNote){
        this.docNote = newNote;
    }

    public String getDocNote(){
        return this.docNote;
    }

    @Override
    public String toString() {
        return ("姓名: " + this.name + System.lineSeparator() + "年龄: " + this.age +  System.lineSeparator() + "性别: " + this.sex + System.lineSeparator() + "--------------" + System.lineSeparator() +
                "体温: " + this.bodyTemp + " ℃" + System.lineSeparator() + "脉搏: " + this.pulse + " /min" + System.lineSeparator() +
                "呼吸: " + this.breath + " /min" + System.lineSeparator() + "血压舒张压: " + this.releasePressure + " mmHg" + System.lineSeparator() +
                "血压收缩压: " + this.tensePressure + " mmHg" + System.lineSeparator() + "血气分析: " + this.bgAnalysis + " pH" + System.lineSeparator() + "血纳: " + this.bloodNa + "mmol/L" + System.lineSeparator() +
                "血钾: " + this.bloodK + "mmol/L" + System.lineSeparator() + "--------------" + System.lineSeparator() +
                "手术类型: " + this.opPending + System.lineSeparator() + "病理报告: " + this.pathologyResult + System.lineSeparator() + "影像学结果: " + this.imagingResult + System.lineSeparator() +
                "医嘱: " + this.docNote + System.lineSeparator());
    }


}
