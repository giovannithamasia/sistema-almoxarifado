package com.senai.sistema_almoxarifado.controller.produto;

import com.senai.sistema_almoxarifado.dto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.service.ProdutoService;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProdutoPageController {

    private final ProdutoService service;

    @GetMapping("/produtolista")
    public String getProdutoLista(@RequestParam(value = "termo", required = false) String termo, HttpSession session, Model model){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        List<ProdutoRespostaDto> listaProdutos;

        if (termo != null && !termo.trim().isEmpty()) {
            listaProdutos = service.buscarPersonalizada(termo);
        } else {
            listaProdutos = service.listarProdutos();
        }

        model.addAttribute("listaProdutos",listaProdutos);

        model.addAttribute("termo", termo);

        return "produtos/produtolista";
    }
}
