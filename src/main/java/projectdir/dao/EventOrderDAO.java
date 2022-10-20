package projectdir.dao;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import projectdir.models.CancelOrder;
import projectdir.models.CreateOrder;
import projectdir.models.Order;
import projectdir.models.Event;

import java.util.List;
import java.util.UUID;

@Component
public class EventOrderDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*Создание агрегата*/
    public void NewOrder(CreateOrder createOrder){

        createOrder.setPriceOrder(jdbcTemplate.queryForObject("SELECT price FROM products WHERE type=?",
                new Object[]{createOrder.getProduct()},  Integer.class)); //Получаем стоимость товара из базы состояния
        /*Создаем дату для базы состояний*/
        JSONObject jsonData = new JSONObject(
                "{\"customer\":\""+createOrder.getCustomer()+"\",\"priceOrder\":\""+createOrder.getPriceOrder()+
                        "\",\"product\":\""+createOrder.getProduct()+"\",\"status\":\""+
                        jdbcTemplate.queryForObject("SELECT event_type FROM event_types WHERE id=1", String.class)+"\"}");

        Event event = new Event();
        event.setOrder_uuid(UUID.randomUUID().toString());
        event.setData(jsonData.toString());
        event.setEvent_id(1);

        jdbcTemplate.update("INSERT INTO orders(start_time, expected_time,status, UUID) " +
                "VALUES (CURRENT_TIMESTAMP, (CURRENT_TIMESTAMP+ INTERVAL '00:15' ),?,?)",
                jdbcTemplate.queryForObject("SELECT event_type FROM event_types WHERE id=?", new Object[]{event.getEvent_id()},  String.class)
                ,event.getOrder_uuid());
        //Создаем заказ в базе. поступает время заказа + ожидаемое время выполнения

        jdbcTemplate.update("INSERT INTO order_events(time, order_uuid, data)\n" +
                "VALUES (CURRENT_TIMESTAMP, ?, ?)",event.getOrder_uuid(),event.getData().toString());
    }

    /*Взять заказ в работу*/
    public void ToProcessOrder(String uuid){
        JSONObject jsonData = new JSONObject("{\"status\":\""+ jdbcTemplate.queryForObject("SELECT event_type FROM event_types WHERE id=2", String.class)+"\"}");
        jdbcTemplate.update("INSERT INTO order_events(time, order_uuid, data)\n" + "VALUES (CURRENT_TIMESTAMP, ?, ?)",
                uuid,
                jsonData.toString());
    }

    /*Готовность заказа*/
    public void ReadyOrder(String uuid) {
        JSONObject jsonData = new JSONObject("{\"status\":\""+ jdbcTemplate.queryForObject("SELECT event_type FROM event_types WHERE id=3", String.class)+"\"}");
        jdbcTemplate.update("INSERT INTO order_events(time, order_uuid, data)\n" + "VALUES (CURRENT_TIMESTAMP, ?, ?)",
                uuid,
                jsonData.toString());
    }

    /*Отмена заказа*/
    public void CancelOrder(String uuid, CancelOrder event) {

        JSONObject jsonData = new JSONObject("{\"comment\":\""+event.getComment()+"\",\"status\":\""+
                jdbcTemplate.queryForObject("SELECT event_type FROM event_types WHERE id=4", String.class)+"\"}");

//        System.out.println( jsonData.get("status"));
//        System.out.println( jsonData.get("comment"));
        jdbcTemplate.update("INSERT INTO order_events(time, order_uuid, data)\n" + "VALUES (CURRENT_TIMESTAMP, ?, ?)",
                uuid,
                jsonData.toString());
    }

    /*Завершение заказа*/
    public void CompleteOrder(String uuid) {
        JSONObject jsonData = new JSONObject("{\"status\":\""+ jdbcTemplate.queryForObject("SELECT event_type FROM event_types WHERE id=5", String.class)+"\"}");
        jdbcTemplate.update("INSERT INTO order_events(time, order_uuid, data)\n" + "VALUES (CURRENT_TIMESTAMP, ?, ?)",
                uuid,
                jsonData.toString());
    }

    /*Для вывода заказов в таблицу*/
    public List<Order> OrderTable(){
        UpdateStateDB();
        return jdbcTemplate.query("SELECT uuid, customer, comment, status,start_time,end_time FROM orders", new BeanPropertyRowMapper<>(Order.class));
    }



    /*Обновление базы состояний по базе событий*/
    public void UpdateStateDB(){

        List<Order> OrderUUID= jdbcTemplate.query("SELECT uuid FROM orders WHERE status!='CANCELED' AND status!='COMPLETED'",
                new BeanPropertyRowMapper<>(Order.class));

        for (Order order:OrderUUID) {
            List<Event> EventsData= jdbcTemplate.query("SELECT data,time FROM order_events WHERE order_uuid=? ORDER BY id ASC",
                    new Object[]{order.getUuid()}, new BeanPropertyRowMapper<>(Event.class));

            for (Event event : EventsData) {
                JSONObject jsonData = new JSONObject(event.getData().replaceAll("\\\\", ""));
                System.out.println("Otmenyaem");
                System.out.println((jsonData.toString()));

                switch (jsonData.get("status").toString()) {
                    case "CREATED": {
                        jdbcTemplate.update("UPDATE orders  SET customer=?, product=?, price=?, status=? WHERE uuid = ?",
                                jsonData.get("customer").toString(),
                                jsonData.get("product").toString(),
                                Integer.parseInt(jsonData.get("priceOrder").toString()),
                                jsonData.get("status"),
                                order.getUuid());
                    }
                    break;
                    case "IN PROCESS":{
                        jdbcTemplate.update("UPDATE orders  SET  status=? WHERE uuid = ?",
                                jsonData.get("status"),
                                order.getUuid());
                    }
                        break;
                    case "READY":{
                        jdbcTemplate.update("UPDATE orders  SET  status=? WHERE uuid = ?",
                                jsonData.get("status"),
                                order.getUuid());
                    }
                        break;
                    case "CANCELED":{
                        jdbcTemplate.update("UPDATE orders  SET  status=?,comment=? WHERE uuid = ?",
                                jsonData.get("status"),
                                jsonData.get("comment"),
                                order.getUuid());

                    }
                        break;
                    case "COMPLETED":{

                        jdbcTemplate.update("UPDATE orders  SET  status=?,end_time=? WHERE uuid = ?",
                                jsonData.get("status"),
                                event.getTime(),
                                order.getUuid());
                    }
                        break;
                    default:
                        break;
                }
            }

        }

    }


}
