package com.pio.roguelike.map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class ASCIIMap {
    Mesh mesh;
    Texture texture;
    ShaderProgram shader;
    byte[] logic_values;
    int width, height;
    int start_x, start_y;
    int tile_w, tile_h;
    final int[] can_move_to = {'.', '#'};
    
    private ArrayList<Object> traps_pos = new ArrayList<>();

    public ASCIIMap(String name, ASCIITextureInfo tex_info) {
        this.shader = new ShaderProgram(Gdx.files.internal("shaders/map.v.glsl"), Gdx.files.internal("shaders/map.f.glsl"));
        this.texture = new Texture(Gdx.files.internal("ascii/fira_mono_medium_24.png"));
        this.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        YamlReader yml_reader = new YamlReader(Gdx.files.internal("maps/" + name + ".yml").reader());
        Map map_info;
        try {
            map_info = (Map)yml_reader.read();
        } catch (YamlException e) {
            throw new RuntimeException("Could not parse map info file!");
        }
        // Nawet gorzej niż w C++… a podobno Java jest wysokopoziomowym językiem (chyba że to po prostu ograniczenia tej
        // biblioteki i w Javie da się napisać coś w stylu `int a = yaml_document["some key"];`)
        this.width = Integer.parseInt((String)map_info.get("width"));
        this.height = Integer.parseInt((String)map_info.get("height"));
        this.start_x = Integer.parseInt((String)map_info.get("start x"));
        this.start_y = Integer.parseInt((String)map_info.get("start y"));

        read_traps(map_info);
        if(test_map_info(map_info)) System.out.println("Dane mapy poprawnie wczytane");
        else{
            System.out.println("Nie udało się wczytać danych mapy");
            System.exit(0);
        }
        
        int img_w = texture.getWidth();
        int img_h = texture.getHeight();
        this.tile_w = tex_info.char_width();
        this.tile_h = tex_info.char_height();

        String map = Gdx.files.internal("maps/" + name).readString();

        // Odwracamy mapę aby ją oprawnie narysować
        String lines[] = map.split("\n");
        map = "";
        for (int i = lines.length - 1; i >= 0; i--) {
            map += lines[i] + "\n";
        }
        this.logic_values = new byte[this.width * this.height];

        // [map_h * map_w * 5 * 6] - wymiary mapy * wielkość wierzchołak * ilość wierzchołków
        float[] vert = new  float[this.width * this.height * 5 * 6];
        int i = 0, w_pos = 0, h_pos = 0, logic_pos = 0;

        for (int map_i = 0; map_i < map.length(); ++map_i) {
            char c = map.charAt(map_i);
            // Jak ktoś zamieni Unixowe zakończenie plików na Windowsowe…
            if (c == '\n') {
                w_pos = 0;
                h_pos++;
                continue;
            }

            if (IntStream.of(can_move_to).anyMatch(x -> x == (int)c)) {
                logic_values[logic_pos] = 0;
            }
            else {
                logic_values[logic_pos] = 1;
            }
            logic_pos++;

            CharMetric met = tex_info.get(c);
            float x = w_pos * this.tile_w + met.offset_x;
            float y = h_pos * this.tile_h + met.height + met.offset_y;

            // Pierwszy trójkąt (wierchołki w polejności - pozycja(x, y, z), tekstura(u, v))
            vert[i++] = x;
            vert[i++] = y;
            vert[i++] = 0;
            vert[i++] = met.pos_x / img_w;
            vert[i++] = (met.pos_y + met.height) / img_h;

            vert[i++] = x;
            vert[i++] = y + met.height;
            vert[i++] = 0;
            vert[i++] = met.pos_x / img_w;
            vert[i++] = met.pos_y / img_h;

            vert[i++] = x + met.width;
            vert[i++] = y;
            vert[i++] = 0;
            vert[i++] = (met.pos_x + met.width) / img_w;
            vert[i++] = (met.pos_y + met.height) / img_h;

            // Drugi trójkąt
            vert[i++] = x;
            vert[i++] = y + met.height;
            vert[i++] = 0;
            vert[i++] = met.pos_x / img_w;
            vert[i++] = met.pos_y / img_h;

            vert[i++] = x + met.width;
            vert[i++] = y;
            vert[i++] = 0;
            vert[i++] = (met.pos_x + met.width) / img_w;
            vert[i++] = (met.pos_y + met.height) / img_h;

            vert[i++] = x + met.width;
            vert[i++] = y + met.height;
            vert[i++] = 0;
            vert[i++] = (met.pos_x + met.width) / img_w;
            vert[i++] = met.pos_y / img_h;

            w_pos++;
        }

        this.mesh = new Mesh(true, vert.length, 0,
                        new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        this.mesh.setVertices(vert);
        for (int j = 0, w = 0; j < logic_values.length; j++, w++) {
            if (w == this.width) {
                w = 0;
            }
        }
    }

    public void render(Matrix4 projection) {
        texture.bind();
        shader.begin();
        shader.setUniformi("u_texture", 0);
        shader.setUniformMatrix("u_projectionViewMatrix", projection);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    /// Sprawdza czy dane pole jest poprawne do przemieszczenia się. Zwraca fałsz gdy już coś na nim się znajduje,
    /// lub gdy podane półżędneą nieprawidłowe.
    public boolean is_valid(int w, int h) {
        // Bo Java nie ma unsigned…
        if (w < 0 || h < 0) {
            return false;
        }
        if (w > width || h > height) {
            return false;
        }
        if (logic_values[w + h * width] != 0) {
            return false;
        }
        return true;
    }

    public int tile_width() { return tile_w; }
    public int tile_height() { return tile_h; }
    public int start_pos_x() { return start_x; }
    public int start_pos_y() { return start_y; }
    
    private void read_traps(Map map_info){
        traps_pos = (ArrayList<Object>) map_info.get("traps");
    }
    
    public ArrayList<Object> get_traps() { return traps_pos; }
    public byte[] get_logic_array() { return logic_values; }

    private boolean test_map_info(Map map_info){
        
        // Sprawdź poprawność koordynatów danych do mapy
        Set mapInfoSet = map_info.keySet();
        Iterator it = mapInfoSet.iterator();
        while(it.hasNext()){
            
            if(it.next().equals("start x")){
                Integer x = (Integer)map_info.get("start x");
                if(!(x instanceof Integer) || x < 0) return false;
            }else if(it.next().equals("start y")){
                Integer x = (Integer)map_info.get("start y");
                if(!(x instanceof Integer) || x < 0) return false;
            }else if(it.next().equals("width")){
                Integer x = (Integer)map_info.get("width");
                if(!(x instanceof Integer) || x < 0) return false;
            }else if(it.next().equals("height")){
                Integer x = (Integer)map_info.get("height");
                if(!(x instanceof Integer) || x < 0) return false;
            }else if(it.next().equals("traps")){
                traps_pos = (ArrayList<Object>) map_info.get("traps");
                Iterator jt = traps_pos.iterator();
                while(jt.hasNext()){
                    HashMap trap_pos = (HashMap)it.next();
                    
                    Integer x = (Integer)trap_pos.get("x");
                    if(!(x instanceof Integer) || x < 0) return false;
                    
                    x = (Integer)trap_pos.get("y");
                    if(!(x instanceof Integer) || x < 0) return false;
                }
            }
        }
        
        return true;
    }
}
