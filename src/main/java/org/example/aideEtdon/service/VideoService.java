package org.example.aideEtdon.service;

import org.example.IService.IService;
import org.example.aideEtdon.model.Video;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoService implements IService<Video> {
    private Connection cnx;

    public VideoService() {
        try {
            cnx = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ajouter(Video video) throws SQLException {
        String req = "INSERT INTO video (title, youtube_url) VALUES (?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, video.getTitle());
        ps.setString(2, video.getYoutubeUrl());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Video video) throws SQLException {
        String req = "UPDATE video SET title=?, youtube_url=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, video.getTitle());
        ps.setString(2, video.getYoutubeUrl());
        ps.setInt(3, video.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM video WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Video> afficher() throws SQLException {
        List<Video> list = new ArrayList<>();
        String req = "SELECT * FROM video";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(new Video(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("youtube_url")
            ));
        }
        return list;
    }
}
