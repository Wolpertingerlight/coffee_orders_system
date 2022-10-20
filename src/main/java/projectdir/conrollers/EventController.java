package projectdir.conrollers;
import org.springframework.web.bind.annotation.*;
import projectdir.dao.EventOrderDAO;
import projectdir.models.CancelOrder;
import projectdir.models.CreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


@Controller
@RequestMapping("/")
public class EventController {

    @Autowired
    private EventOrderDAO eventOrderDAO;

    @Autowired
    public EventController(EventOrderDAO eventOrderDAO) {
        this.eventOrderDAO = eventOrderDAO;
    }



    @GetMapping()

    public  String OrderTable(Model model){
        model.addAttribute("ordertable", eventOrderDAO.OrderTable());
        return"index";
    }

    @PostMapping()
    public String newOrder(@ModelAttribute("event") CreateOrder event){
        eventOrderDAO.NewOrder(event);
        return "redirect:/";
    }

    @GetMapping("/toprocess/{uuid}")
    public String toProcessOrder(@PathVariable("uuid") String uuid, Model model){
        eventOrderDAO.ToProcessOrder(uuid);
        return "redirect:/";
    }

    @GetMapping("/ready/{uuid}")
    public String readyOrder(@PathVariable("uuid") String uuid, Model model){
        eventOrderDAO.ReadyOrder(uuid);
        return "redirect:/";
    }

    @GetMapping("/complete/{uuid}")
    public String CompleteOrder(@PathVariable("uuid") String uuid, Model model){
        eventOrderDAO.CompleteOrder(uuid);
        return "redirect:/";
    }

    @GetMapping("/cancelform")
    public String CancelOrder(@RequestParam(value = "uuid", required = false) String uuid, Model model ){
        model.addAttribute("uuid", uuid);
        return "cancelform";
    }

    @PostMapping("/cancel")
    public String CancelOrder(@RequestParam(value = "uuid", required = false) String uuid, @ModelAttribute("cancel") CancelOrder event){
        eventOrderDAO.CancelOrder(uuid,event);
        return "redirect:/";
    }


}
