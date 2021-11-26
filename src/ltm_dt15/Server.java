/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ltm_dt15;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import model.SinhVien;
import model.SqlAuth;

/**
 *
 * @author admin
 */
public class Server {

    private static DatagramPacket getData(DatagramSocket socket) throws IOException {
        byte[] dataPacket = new byte[1000];
        DatagramPacket packet = new DatagramPacket(dataPacket, dataPacket.length);
        socket.receive(packet);
        return packet;
    }

    private static DatagramPacket sendData(String reply, InetAddress inet, int port, DatagramSocket socket) throws IOException {
        byte[] replyByte = reply.getBytes();
        DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, inet, port);
        socket.send(replyPacket);
        return replyPacket;
    }

    public static void main(String[] args) throws SocketException, IOException, Exception {
        InetAddress inet = InetAddress.getByName("localhost");
        int port = 8888;
        DatagramSocket socket = new DatagramSocket(port, inet);
        System.out.println("Server dang chay!!!");

        byte[] dataPacket;
        DatagramPacket packet;
        String line;
        ByteArrayInputStream in;
        ObjectInputStream is;
        DBAccess db = null;
        ObjectMapper mapper = new ObjectMapper();
        SinhVien sv = new SinhVien();
        DES des = new DES();

        OUTER:
        while (true) {
            dataPacket = new byte[1000];
            packet = new DatagramPacket(dataPacket, dataPacket.length);
            socket.receive(packet);
            line = new String(packet.getData(), 0, packet.getLength());
            System.out.println(line);
            switch (line) {
                case "connectSQL":
                    packet = getData(socket);
                    in = new ByteArrayInputStream(packet.getData());
                    is = new ObjectInputStream(in);

                    try {
                        SqlAuth sql = (SqlAuth) is.readObject();
                        db = new DBAccess();
                        db.connect("jdbc:sqlserver://" + sql.getIp() + ":" + sql.getPort() + ";Database=LapTrinhMang;user=" + sql.getUser() + ";password=" + sql.getPass());
                        //db.connect("jdbc:sqlserver://localhost:1433;Database=NCKH;user=sa;password=123456");
                        sendData("success", packet.getAddress(), packet.getPort(), socket);

                        //Gửi danh sách sinh viên sang Home
                        ArrayList<SinhVien> list = new ArrayList<SinhVien>();
                        ResultSet rs = db.Query("SELECT * FROM SinhVien");
                        while (rs.next()) {
                            SinhVien item = new SinhVien();
                            String hoten = des.decrypt(rs.getString("hoten"));
                            String mssv = des.decrypt(rs.getString("mssv"));
                            String toan = des.decrypt(rs.getString("toan"));
                            String van = des.decrypt(rs.getString("van"));
                            String anh = des.decrypt(rs.getString("anh"));

                            item.setId(rs.getInt("id"));
                            item.setHoten(hoten);
                            item.setMssv(mssv);
                            item.setToan(Double.parseDouble(toan));
                            item.setVan(Double.parseDouble(van));
                            item.setAnh(Double.parseDouble(anh));
                            list.add(item);
                        }
                        sendData(mapper.writeValueAsString(list), packet.getAddress(), packet.getPort(), socket);

                    } catch (ClassNotFoundException | SQLException ex) {
                        sendData(ex.getMessage(), packet.getAddress(), packet.getPort(), socket);
                    }

                    break;
                case "info_sv": // @author PHONG
                    packet = getData(socket);
                    // xu lieu du lieu duoc nhan
                    String duLieuNhanDuocTuDangKy = new String(packet.getData(), 0, packet.getLength());

                    // tách du lieu và mã hóa
                    String[] mangDuLieuNhanDuoc = duLieuNhanDuocTuDangKy.split("#");
                    String hoten = des.encrypt(mangDuLieuNhanDuoc[0]);
                    String mssv = des.encrypt(mangDuLieuNhanDuoc[1]);
                    String toan = des.encrypt(mangDuLieuNhanDuoc[2]);
                    String van = des.encrypt(mangDuLieuNhanDuoc[3]);
                    String anh = des.encrypt(mangDuLieuNhanDuoc[4]);

                    try {
                        //kiem tra xem ma nhan vien co bi trung - neu trung thi dung lai luon
                        ResultSet rs = db.Query("SELECT * FROM SinhVien WHERE mssv = '" + mssv + "'");
                        if (rs.next()) {
                            sendData("duplicate_masv", packet.getAddress(), packet.getPort(), socket);
                            break;
                        }
                    } catch (SQLException ex) {
                        sendData(ex.getMessage(), packet.getAddress(), packet.getPort(), socket);
                    }

                    // neu ma sinh vien khong bi trung thi tiep tuc them moi vao co so du lieu
                    String cauTruyVan = "INSERT INTO SinhVien( hoten, mssv, toan, van, anh) "
                            + "VALUES( '" + hoten + "' , '" + mssv + "', '" + toan + "', '" + van + "', '" + anh + "')";

                    db.Query(cauTruyVan);
                    sendData("success", packet.getAddress(), packet.getPort(), socket);

                    //lấy dữ liệu từ DB và giải mã
                    try {
                        ArrayList<SinhVien> list = new ArrayList<SinhVien>();
                        ResultSet rs = db.Query("SELECT * FROM SinhVien");
                        while (rs.next()) {
                            SinhVien item = new SinhVien();
                            //giải mã
                            hoten = des.decrypt(rs.getString("hoten"));
                            mssv = des.decrypt(rs.getString("mssv"));
                            toan = des.decrypt(rs.getString("toan"));
                            van = des.decrypt(rs.getString("van"));
                            anh = des.decrypt(rs.getString("anh"));
                            //
                            item.setId(rs.getInt("id"));
                            item.setHoten(hoten);
                            item.setMssv(mssv);
                            item.setToan(Double.parseDouble(toan));
                            item.setVan(Double.parseDouble(van));
                            item.setAnh(Double.parseDouble(anh));
                            list.add(item);
                        }
                        sendData(mapper.writeValueAsString(list), packet.getAddress(), packet.getPort(), socket);
                    } catch (SQLException ex) {
                        System.out.println("HEHE");
                        //sendData(ex.getMessage(), packet.getAddress(), packet.getPort(), socket);
                    }
                    break;
                case "demo2":

                    break;
                case "demo3":
                    break;
                case "QUIT":
                    break OUTER;
            }
        }

        System.out.println("Server da dung lai!!");
        socket.close();
    }
}
