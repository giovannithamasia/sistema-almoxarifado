package com.senai.sistema_almoxarifado.controller.produto;

import com.senai.sistema_almoxarifado.dto.produto.ProdutoAtualizarDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoDto;
import com.senai.sistema_almoxarifado.exceptions.ProdutoCadastradoException;
import com.senai.sistema_almoxarifado.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/produtocadastrar")
    public String cadastrarProduto(@Valid @ModelAttribute("produtoDto") ProdutoDto produtoDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {

        if (bindingResult.hasErrors()) {
            return "produtos/produtocadastrar";
        }

        try {
            produtoService.cadastrarProduto(produtoDto);
            redirectAttributes.addFlashAttribute("mensagemCriacao", "Produto cadastrado com sucesso!");
            return "redirect:/produtolista";
        } catch (ProdutoCadastradoException ex) {
            model.addAttribute("erroCodigoDuplicado", ex.getMessage());
            return "produtos/produtocadastrar";
        }
    }

    @PostMapping("/produtoatualizar/{id}")
    public String atualizarProduto(@PathVariable("id") Long id,
                                   @Valid @ModelAttribute("produtoAtualizacao")
                                   ProdutoAtualizarDto produtoAtualizarDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "produtos/produtoatualizar";
        }

        produtoService.atualizarProduto(id, produtoAtualizarDto);

        redirectAttributes.addFlashAttribute("mensagemAtualizacao", "Produto atualizado com sucesso!");
        return "redirect:/produtolista";
    }

    @DeleteMapping("/produtoexcluir/{id}")
    public ResponseEntity<String> excluir(@PathVariable("id") Long id) {
        produtoService.excluirProduto(id);
        return ResponseEntity.noContent().build();
    }
}