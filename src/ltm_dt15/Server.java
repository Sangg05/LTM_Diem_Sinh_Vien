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

    public static void main(String[] args) throws SocketException, IOException {
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
                    } catch (ClassNotFoundException | SQLException ex) {
                        sendData(ex.getMessage(), packet.getAddress(), packet.getPort(), socket);
                    }
                    break;
                case "info_sv": // @author PHONG
                    packet = getData(socket);

                    // xu lieu du lieu duoc nhan
                    String duLieuNhanDuocTuDangKy = new String(packet.getData(), 0, packet.getLength());

                    System.out.println(duLieuNhanDuocTuDangKy);

                    String[] mangDuLieuNhanDuoc = duLieuNhanDuocTuDangKy.split("#");

                    // lay du lieu ra ngoai
                    String hoten = mangDuLieuNhanDuoc[0];
                    String mssv = mangDuLieuNhanDuoc[1];
                    String toan = mangDuLieuNhanDuoc[2];
                    String van = mangDuLieuNhanDuoc[3];
                    String anh = mangDuLieuNhanDuoc[4];

                    try {
                        //kiem tra xem ma nhan vien co bi trung - neu trung thi dung lai luon
                        ResultSet rs = db.Query("SELECT * FROM DiemSinhVien WHERE mssv = '" + mssv + "'");
                        if (rs.next()) {
                            sendData("duplicate_masv", packet.getAddress(), packet.getPort(), socket);
                            break;
                        }
                    } catch (SQLException ex) {
                        sendData(ex.getMessage(), packet.getAddress(), packet.getPort(), socket);
                    }

                    // neu ma sinh vien khong bi trung thi tiep tuc them moi vao co so du lieu
                    String cauTruyVan = "INSERT INTO DiemSinhVien( hoten, mssv, toan, van, anh) "
                            + "VALUES( '" + hoten + "' , '" + mssv + "', " + toan + ", " + van + ", " + anh + ")";

                    System.out.println("cauTruyVan");

                    db.Query(cauTruyVan);
                    sendData("success", packet.getAddress(), packet.getPort(), socket);
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
