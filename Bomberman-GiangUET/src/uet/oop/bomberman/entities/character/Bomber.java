package uet.oop.bomberman.entities.character;

import java.awt.Color;
import java.util.ArrayList;
import uet.oop.bomberman.Board;
import uet.oop.bomberman.Game;
import uet.oop.bomberman.entities.Entity;
import uet.oop.bomberman.entities.bomb.Bomb;
import uet.oop.bomberman.graphics.Screen;
import uet.oop.bomberman.graphics.Sprite;
import uet.oop.bomberman.input.Keyboard;

import java.util.Iterator;
import java.util.List;
import uet.oop.bomberman.entities.LayeredEntity;
import uet.oop.bomberman.entities.Message;
import uet.oop.bomberman.entities.bomb.Flame;
import uet.oop.bomberman.entities.character.enemy.Enemy;
import uet.oop.bomberman.entities.tile.Wall;
import uet.oop.bomberman.entities.tile.destroyable.Brick;
import uet.oop.bomberman.entities.tile.item.BombItem;
import uet.oop.bomberman.entities.tile.item.Item;
import uet.oop.bomberman.level.Coordinates;
import uet.oop.bomberman.music.Sound;

public class Bomber extends Character {

    private final List<Bomb> _bombs;
    protected Keyboard _input;

    /**
     * nếu giá trị này < 0 thì cho phép đặt đối tượng Bomb tiếp theo, cứ mỗi lần
     * đặt 1 Bomb mới, giá trị này sẽ được reset về 0 và giảm dần trong mỗi lần
     * update()
     */
    protected int _timeBetweenPutBombs = 0;
    public static int _bombRateNow = 0;
    public static List<Item> _items = new ArrayList<Item>();

    public Bomber(int x, int y, Board board) {
        super(x, y, board);
        _bombs = _board.getBombs();
        _input = _board.getInput();
        _sprite = Sprite.player_right;
    }

    @Override
    public void update() {
        clearBombs();
        if (!_alive) {
            afterKill();
            return;
        }

        if (_timeBetweenPutBombs < -7500) {
            _timeBetweenPutBombs = 0;
        } else {
            _timeBetweenPutBombs--;
        }

        animate();

        calculateMove();

        detectPlaceBomb();
    }

    @Override
    public void render(Screen screen) {
        calculateXOffset();

        if (_alive) {
            chooseSprite();
        } else {
            _sprite = Sprite.player_dead1;
        }

        screen.renderEntity((int) _x, (int) _y - _sprite.SIZE, this);
    }

    public void calculateXOffset() {
        int xScroll = Screen.calculateXOffset(_board, this);
        Screen.setOffset(xScroll, 0);
    }

    /**
     * Kiểm tra xem có đặt được bom hay không? nếu có thì đặt bom tại vị trí
     * hiện tại của Bomber
     */
    private void detectPlaceBomb() {
        // TODO: kiểm tra xem phím điều khiển đặt bom có được gõ và giá trị _timeBetweenPutBombs, Game.getBombRate() có thỏa mãn hay không
        // TODO:  Game.getBombRate() sẽ trả về số lượng bom có thể đặt liên tiếp tại thời điểm hiện tại
        // TODO: _timeBetweenPutBombs dùng để ngăn chặn Bomber đặt 2 Bomb cùng tại 1 vị trí trong 1 khoảng thời gian quá ngắn
        // TODO: nếu 3 điều kiện trên thỏa mãn thì thực hiện đặt bom bằng placeBomb()
        // TODO: sau khi đặt, nhớ giảm số lượng Bomb Rate và reset _timeBetweenPutBombs về 0
        if (_input.space && Game.getBombRate() > 0 && _timeBetweenPutBombs < 0) {

            int xt = Coordinates.pixelToTile(_x + _sprite.getSize() / 2);
            int yt = Coordinates.pixelToTile((_y + _sprite.getSize() / 2) - _sprite.getSize()); //subtract half player height and minus 1 y position

            placeBomb(xt, yt);
            Game.addBombRate(-1);

            _timeBetweenPutBombs = 30;
        }
    }

    protected void placeBomb(int x, int y) {
        // TODO: thực hiện tạo đối tượng bom, đặt vào vị trí (x, y)
        Bomb b = new Bomb(x, y, _board);
        _board.addBomb(b);
    }

    private void clearBombs() {
        Iterator<Bomb> bs = _bombs.iterator();

        Bomb b;
        while (bs.hasNext()) {
            b = bs.next();
            if (b.isRemoved()) {
                bs.remove();
                Game.addBombRate(1);
            }
        }

    }

    @Override
    public void kill() {
        if (!_alive) {
            return;
        }
        _alive = false;
        _board.subLives(1);
        Message message = new Message("-1 LIVE", getXMessage(), getYMessage(), 2, Color.white, 14);
        _board.addMessage(message);
        Sound die = new Sound("res\\audio\\audioBomberDead.wav");
        die.play();
    }

    @Override
    protected void afterKill() {
        if (_timeAfter > 0) {
            --_timeAfter;
        } else {
            if (_board.getLives() == 0) {
                _board.endGame();
            } else {
                _board.restartLevel();
                Game.bombRate = 1;
            }
        }
    }

    @Override
    protected void calculateMove() {
        // TODO: xử lý nhận tín hiệu điều khiển hướng đi từ _input và gọi move() để thực hiện di chuyển
        // TODO: nhớ cập nhật lại giá trị cờ _moving khi thay đổi trạng thái di chuyển
        double xa = 0, ya = 0;
        if (_input.right) { // sang phải
            xa += Game.bomberSpeed;
        } else if (_input.left) { // sang trái
            xa -= Game.bomberSpeed;
        } else if (_input.up) { // lên trên
            ya -= Game.bomberSpeed;
        } else if (_input.down) { // xuống dưới
            ya += Game.bomberSpeed;
        }

        // TODO: nhớ cập nhật lại giá trị cờ _moving khi thay đổi trạng thái di chuyển
        if (xa != 0 || ya != 0) {
            move(xa, ya); // gọi hàm move() để di chuyển
            _moving = true; // thay đổi cờ
        } else {
            _moving = false; // thay đổi cờ
        }
//        _moving = true;
//
//        if (_input.up) {
//            move(0, -Game.getBomberSpeed());
//        } else if (_input.down) {
//            move(0, Game.getBomberSpeed());
//        } else if (_input.left) {
//            move(-Game.getBomberSpeed(), 0);
//        } else if (_input.right) {
//            move(Game.getBomberSpeed(), 0);
//        } else {
//            _moving = false;
//        }
    }

    public void moveCenterX() {
        int pixelOfEntity = Coordinates.tileToPixel(1);
        double centerX = _x + _sprite.getRealWidth() / 2;
        int tileCenterX = Coordinates.pixelToTile(centerX);
        _x = Coordinates.tileToPixel(tileCenterX) + pixelOfEntity / 2 - _sprite.getRealWidth() / 2;
    }

    public void moveCenterY() {
        int pixelOfEntity = Coordinates.tileToPixel(1);
        double centerY = _y - _sprite.getRealHeight() / 2;
        int tileCenterY = Coordinates.pixelToTile(centerY);
        _y = Coordinates.tileToPixel(tileCenterY) + pixelOfEntity / 2 + _sprite.getRealHeight() / 2;
    }

    public void autoMoveCenter() {
        int pixelOfEntity = Coordinates.tileToPixel(1);
        double centerX = _x + _sprite.getRealWidth() / 2;
        double centerY = _y - _sprite.getRealHeight() / 2;

        boolean contactTop = !canMove(centerX, centerY - pixelOfEntity / 2);
        boolean contactDown = !canMove(centerX, centerY + pixelOfEntity / 2);
        boolean contactLeft = !canMove(centerX - pixelOfEntity / 2, centerY);
        boolean contactRight = !canMove(centerX + pixelOfEntity / 2, centerY);

        // Các trường hợp đi một nửa người vào tường cũng tự động căn giữa.
        if (_direction != 0 && contactDown) {
            moveCenterY();
        }
        if (_direction != 1 && contactLeft) {
            moveCenterX();
        }
        if (_direction != 2 && contactTop) {
            moveCenterY();
        }
        if (_direction != 3 && contactRight) {
            moveCenterX();
        }
    }

    @Override
    public boolean canMove(double x, double y) {
        // TODO: kiểm tra có đối tượng tại vị trí chuẩn bị di chuyển đến và có thể di chuyển tới đó hay không
//        for (int c = 0; c < 4; c++) { //kiểm tra tại 4 góc có vật cản hay không
//            int xt = Coordinates.pixelToTile((_x + x) + c % 2 * 11);
//            int yt = Coordinates.pixelToTile((_y + y) + c / 2 * 12 - 13);
//
//            // Tạo một thực thể ảo tại vị trí xt, yt để kiểm tra
//            Entity a = _board.getEntity(xt, yt, this);
//
//            // Nếu thực thể a KO được phép đi qua (collide = false) thì không thể di chuyển
//            if (!a.collide(this)) {
//                return false;
//            }
//        }
//        return true;
        int tileX = Coordinates.pixelToTile(x);
        int tileY = Coordinates.pixelToTile(y);
//        System.out.println(tileX + " " + tileY);
        Entity nextEntity = _board.getEntity(tileX, tileY, this);
        return collide(nextEntity);
    }

    @Override
    public void move(double xa, double ya) {
        // TODO: sử dụng canMove() để kiểm tra xem có thể di chuyển tới điểm đã tính toán hay không và thực hiện thay đổi tọa độ _x, _y
        // TODO: nhớ cập nhật giá trị _direction sau khi di chuyển
        // TODO: Di chuyển nhân vật ra giữa.
//        if (xa > 0) {
//            _direction = 1;
//        }
//        if (xa < 0) {
//            _direction = 3;
//        }
//        if (ya > 0) {
//            _direction = 2;
//        }
//        if (ya < 0) {
//            _direction = 0;
//        }
//
//        if (canMove(0, ya)) {
//            _y += ya * Game.getBomberSpeed();
//        }
//
//        if (canMove(xa, 0)) {
//            _x += xa * Game.getBomberSpeed();
//        }
        // Tính tọa độ tâm người
        double centerX = _x + _sprite.getRealWidth() / 2;
        double centerY = _y - _sprite.getRealHeight() / 2;

        if (xa > 0) {
            _direction = 1;
        }
        if (xa < 0) {
            _direction = 3;
        }
        if (ya > 0) {
            _direction = 2;
        }
        if (ya < 0) {
            _direction = 0;
        }
        if (canMove(centerX + xa, centerY + ya)) {
            _x += xa;
            _y += ya;
        }

        autoMoveCenter();
    }

    @Override
    public boolean collide(Entity e) {
        // TODO: xử lý va chạm với Flame
        // TODO: xử lý va chạm với Enemy
        if (e instanceof Flame) {
            kill();
            return false;
        }
        // TODO: xử lý va chạm với Enemy
        if (e instanceof Enemy) {
            kill();
            return false;
        }
        if (e instanceof Wall) {
            return false;
        }
        if (e instanceof Brick) {
            return false;
        }
        if (e instanceof LayeredEntity) {
            return e.collide(this);
        }
        return true;
    }

    private void chooseSprite() {
        switch (_direction) {
            case 0:
                _sprite = Sprite.player_up;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_up_1, Sprite.player_up_2, _animate, 20);
                }
                break;
            case 1:
                _sprite = Sprite.player_right;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_right_1, Sprite.player_right_2, _animate, 20);
                }
                break;
            case 2:
                _sprite = Sprite.player_down;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_down_1, Sprite.player_down_2, _animate, 20);
                }
                break;
            case 3:
                _sprite = Sprite.player_left;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_left_1, Sprite.player_left_2, _animate, 20);
                }
                break;
            default:
                _sprite = Sprite.player_right;
                if (_moving) {
                    _sprite = Sprite.movingSprite(Sprite.player_right_1, Sprite.player_right_2, _animate, 20);
                }
                break;
        }
    }

    /*
	|--------------------------------------------------------------------------
	| Powerups
	|--------------------------------------------------------------------------
     */
    public void addItem(Item p) {
        if (p.isRemoved()) {
            return;
        }
        _items.add(p);
        p.setValues();
        Sound itemSound = new Sound("res\\audio\\audioGetItem.wav");
        itemSound.play();
        if (p instanceof BombItem) {
            _bombRateNow = Game.getBombRate();
        }
    }

    public void clearUsedItems() {
        Item p;
        for (int i = 0; i < _items.size(); i++) {
            p = _items.get(i);
            if (p.isActive() == false) {
                _items.remove(i);
            }
        }
    }

    public void removeItems() {
        for (int i = 0; i < _items.size(); i++) {
            _items.remove(i);
        }
    }

}
