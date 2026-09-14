package com.senai.sistema_almoxarifado.controller.movimentacao;

import com.senai.sistema_almoxarifado.service.MovimentacaoEstoqueService;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService service;

    @GetMapping("/movimentacoes")
    public String getMovimentacaoLista(HttpSession session, Model model){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        model.addAttribute("listaMovimentacoes",service.listarProdutosCadastrados());

        model.addAttribute("usuarioLogado", SessaoUtil.obterSessao(session));

        return "movimentacoes/listarmovimentacoes";
    }
}
