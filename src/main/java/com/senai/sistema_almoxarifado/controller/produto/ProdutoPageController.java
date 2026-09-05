package com.senai.sistema_almoxarifado.controller.produto;

import com.senai.sistema_almoxarifado.dto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.service.ProdutoService;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProdutoPageController {

    private final ProdutoService service;

    @GetMapping("/produtolista")
    public String getProdutoLista(HttpSession session, Model model){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        List<ProdutoRespostaDto> listaProdutos = service.listarProdutos();

        model.addAttribute("listaProdutos",listaProdutos);

        return "produtos/produtolista";
    }
}
